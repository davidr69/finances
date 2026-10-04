package com.lavacro.finances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories(basePackages = {"com.lavacro.finances.repositories.jdbc", "com.lavacro.finances.domain.action"})
public class MainApp {
	static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(MainApp.class, args);
		if (ctx.getEnvironment().acceptsProfiles(Profiles.of("reconcile"))) {
			System.exit(SpringApplication.exit(ctx));
		}
	}
}
