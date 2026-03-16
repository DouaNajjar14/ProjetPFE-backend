package com.example.gestion.des.stagiaires;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionDesStagiairesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDesStagiairesApplication.class, args);
	}

}
