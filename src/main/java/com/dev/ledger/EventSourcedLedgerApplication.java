package com.dev.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class EventSourcedLedgerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventSourcedLedgerApplication.class, args);
	}

}
