package com.company.txn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class TxnProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxnProcessingServiceApplication.class, args);
	}

}
