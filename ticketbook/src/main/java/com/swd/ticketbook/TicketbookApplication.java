package com.swd.ticketbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // FR16: Enable background job for order expiry cleanup
public class TicketbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketbookApplication.class, args);
	}

}
