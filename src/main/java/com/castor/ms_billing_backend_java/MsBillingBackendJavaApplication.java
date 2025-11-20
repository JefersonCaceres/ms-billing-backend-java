package com.castor.ms_billing_backend_java;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MsBillingBackendJavaApplication {
	public static void main(String[] args) {
		SpringApplication.run(MsBillingBackendJavaApplication.class, args);
	}
}
