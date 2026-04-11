package com.phelim.system.love_certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.phelim.system.love_certificate.client")
public class LoveCertificateApplication {

	public static void main(String[] args) {
		// Disable openhtmltopdf log
		System.setProperty("xr.util-logging.loggingEnabled", "false");

		SpringApplication.run(LoveCertificateApplication.class, args);
	}

}
