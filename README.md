# Event-Sourced Ledger

An event-sourced, double-entry bank ledger core built with Spring Boot. Every balance change is
derived from an immutable, append-only log of transactions and entries — nothing is ever updated
or deleted, so the current state of any account can always be reconstructed by replaying its
history.

## Core concepts

**Double-entry accounting.** Every transaction is made up of two or more *legs* (entries), each
posted as a debit (`DR`) or credit (`CR`) against an account. Within each currency present in a
transaction, debits and credits must net to zero — the ledger rejects anything that doesn't
balance.

**Normal sides.** Every account has a type, and each type has a normal side that determines
whether a debit or credit *increases* its balance:

| Account type  | Normal side | A `DR` leg... | A `CR` leg... |
|---------------|-------------|----------------|----------------|
| `ASSET`       | Debit       | increases      | decreases      |
| `EXPENSE`     | Debit       | increases      | decreases      |
| `FX_CLEARING` | Debit       | increases      | decreases      |
| `LIABILITY`   | Credit      | decreases      | increases      |
| `EQUITY`      | Credit      | decreases      | increases      |
| `REVENUE`     | Credit      | decreases      | increases      |

**Append-only storage.** `ledger_transaction` and `entry` rows are never updated or deleted —
Postgres rules on both tables silently no-op any `UPDATE`/`DELETE`. To undo a transaction you post
a **reversal**: a new transaction with every leg's direction flipped, linked back to the original
via `reverse_transaction_id`.

**Snapshots.** Replaying every entry ever posted to compute a balance doesn't scale. A background
scheduler periodically persists an `account_snapshot` (balance as of a given transaction
sequence), and balance reads fold only the entries posted *after* the latest snapshot on top of it.

**Idempotency.** Transaction and transfer writes accept an `Idempotency-Key` header. Replaying the
same key returns the original transaction instead of creating a duplicate, backed by a unique
constraint on `ledger_transaction.idempotency_key`.

**Concurrency control.** Writes lock the affected accounts' balance rows and run at
`SERIALIZABLE` isolation, with automatic retry (`spring-retry`, 5 attempts, exponential backoff)
on transient concurrency failures.

## Tech stack

- Java 25, Spring Boot 3.5 (Web, Data JPA, Validation, AOP, Retry)
- PostgreSQL, with schema migrations managed by Flyway
- Maven (wrapper included — no local Maven install required)

## Getting started

### Prerequisites

- JDK 25
- A running PostgreSQL instance

### Database

Create the database referenced in `src/main/resources/application.yaml`:

```sql
CREATE DATABASE ledger;
```

By default the app connects to `jdbc:postgresql://localhost:5432/ledger` with user `postgres`.
Override via environment/JVM properties or by editing `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ledger
    username: postgres
    password: root
```

Flyway runs automatically on startup and applies the migrations in
`src/main/resources/db/migration`.

### Run

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`.

### Test

```bash
./mvnw test
```

### Build a jar

```bash
./mvnw clean package
java -jar target/event-sourced-ledger-0.0.1-SNAPSHOT.jar
```

## Configuration

| Property                          | Default | Description                                              |
|------------------------------------|---------|------------------------------------------------------------|
| `ledger.snapshot.interval-ms`      | 300000  | Delay between scheduled snapshot sweeps                    |
| `ledger.snapshot.initial-delay-ms` | 300000  | Delay before the first snapshot sweep after startup        |

## API

All endpoints accept/return JSON. Validation errors and domain errors are returned as
[RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) `ProblemDetail` bodies.

### Accounts

**Create an account**

```
POST /accounts/create
{
  "name": "Customer Wallet",
  "type": "ASSET",
  "currency": "USD",
  "creditLimit": 0
}
```

`type` is one of `ASSET`, `LIABILITY`, `EQUITY`, `REVENUE`, `EXPENSE`, `FX_CLEARING`.
`creditLimit` (minor units) is how far the balance is allowed to go negative.

**Get an account**

```
GET /accounts/{id}
```

### Transactions

**Post a transaction**

```
POST /transactions
Idempotency-Key: <optional client-generated key>
{
  "type": "DEPOSIT",
  "effectiveAt": "2026-08-01T12:00:00Z",
  "legs": [
    { "accountId": "...", "direction": "DR", "amount": 10000, "currency": "USD" },
    { "accountId": "...", "direction": "CR", "amount": 10000, "currency": "USD" }
  ],
  "metadata": { "note": "optional freeform metadata" }
}
```

- At least two legs are required.
- `amount` is in minor units (cents) and must be positive.
- Legs must net to zero per currency, or the request is rejected with `422`.
- Posting against an account beyond its credit limit is rejected with `409`.
- `effectiveAt` defaults to now if omitted.

**Reverse a transaction**

```
POST /transactions/{id}/reversal
Idempotency-Key: <optional>
{ "reason": "optional" }
```

Creates a new transaction with every original leg's direction flipped.

**Get a transaction**

```
GET /transactions/{id}
```

### Transfers

A convenience endpoint that builds a balanced transaction between two accounts for you, handling
cross-currency conversion automatically.

```
POST /transfers
Idempotency-Key: <optional>
{
  "fromAccountId": "...",
  "toAccountId": "...",
  "amount": 5000,
  "currency": "USD",
  "type": "TRANSFER",
  "metadata": {}
}
```

- Same-currency transfers post a direct two-leg transaction.
- Cross-currency transfers are routed through per-currency `FX_CLEARING` suspense accounts (one
  pair of legs converts source currency into clearing, another converts clearing into the
  destination currency), using the latest rate from the exchange rate service. Clearing accounts
  are provisioned lazily, one per currency, the first time they're needed.

### Balances & audit

**Current or as-of balance**

```
GET /accounts/{id}/balance
GET /accounts/{id}/balance?asOf=2026-07-01T00:00:00Z
```

Without `asOf`, the balance is computed from the latest snapshot plus any entries since. With
`asOf`, the full entry history up to that point in time is folded (no snapshot shortcut).

**Audit trail**

```
GET /accounts/{id}/audit?from=2026-07-01T00:00:00Z&to=2026-08-01T00:00:00Z
```

Returns the opening balance, closing balance, and a step-by-step walk of every entry in the
window — each with a running balance, its transaction's counterparty legs, and a plain-English
explanation.

### Exchange rates

**Set a rate**

```
POST /exchange-rates
{ "baseCurrency": "USD", "quoteCurrency": "EUR", "rate": 0.92 }
```

Rates are append-only (never updated), so setting a new rate for a pair just adds a newer row;
lookups always take the most recent one.

**Get a rate**

```
GET /exchange-rates/{base}/{quote}
```

If no direct rate is found, the inverse of the reverse pair is used automatically.

## Error handling

| Exception                        | HTTP status |
|-----------------------------------|-------------|
| `AccountNotFoundException`        | 404         |
| `TransactionNotFoundException`    | 404         |
| `ExchangeRateNotFoundException`   | 404         |
| `OverdraftException`              | 409         |
| `UnbalancedTransactionException`  | 422         |
| `CurrencyMismatchException`       | 422         |
| `InvalidTransactionException`     | 422         |

## Project layout

```
src/main/java/com/dev/ledger
├── controller   REST endpoints + global exception handling
├── domain       Value types and commands (Money, Side, AccountType, PostTransactionCommand, AuditResult)
├── dto          Request/response records
├── entity       JPA entities
├── exception    Domain exceptions
├── repository   Spring Data repositories
└── service      Business logic (posting, transfers, balances, snapshots, FX, audit)

src/main/resources/db/migration   Flyway migrations
```