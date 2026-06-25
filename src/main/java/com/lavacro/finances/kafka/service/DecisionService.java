package com.lavacro.finances.kafka.service;

import com.lavacro.finances.kafka.model.DecisionModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class DecisionService {
	private final KafkaTemplate<String, DecisionModel> kafkaTemplate;

	public DecisionService(KafkaTemplate<String, DecisionModel> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void send(String filename, Integer accountId, byte[] content) {
		DecisionModel decision = new DecisionModel();
		ProducerRecord<String, DecisionModel> rekord = new ProducerRecord<>("finances-decision", null, decision);

		kafkaTemplate.send(rekord)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					log.error("Failed to send message: {}", ex.getMessage());
				} else {
					log.info("Sent; partition: {}, offset: {}",
						result.getRecordMetadata().partition(),
						result.getRecordMetadata().offset());
				}
			})
		;
	}
}
