package com.lavacro.finances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;

@SpringBootApplication
public class MainApp {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(MainApp.class, args);
		if (ctx.getEnvironment().acceptsProfiles(Profiles.of("reconcile"))) {
			System.exit(SpringApplication.exit(ctx));
		}
	}
}
