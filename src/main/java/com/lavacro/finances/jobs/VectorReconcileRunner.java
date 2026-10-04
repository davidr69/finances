package com.lavacro.finances.jobs;

import com.lavacro.finances.kafka.service.DecisionService;
import com.lavacro.finances.services.EntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("reconcile")
@RequiredArgsConstructor
@Slf4j
public class VectorReconcileRunner implements ApplicationRunner, ExitCodeGenerator {
	private final EntityService entityService;
	private final DecisionService decisionService;
	private int exitCode = 0;

	@Override
	public void run(ApplicationArguments args) {
		try {
			List<Integer> ids = entityService.findEntitiesNeedingVectorSync();
			log.info("Vector reconcile: {} candidate(s)", ids.size());
			for (Integer id : ids) {
				decisionService.generateVector(id);
			}
			decisionService.flush();
		} catch (Exception e) {
			log.error("Vector reconcile failed: {}", e.getMessage(), e);
			exitCode = 1;
		}
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
