package com.lavacro.finances.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class NotifyAgent {
	private final KafkaTemplate<String, byte[]> kafkaTemplate;

	public NotifyAgent(KafkaTemplate<String, byte[]> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void send(String filename, Integer accountId, byte[] content) {
		ProducerRecord<String, byte[]> rekord = new ProducerRecord<>("finances-topic", null, content);
		rekord.headers()
			.add("filename", filename.getBytes(StandardCharsets.UTF_8))
			.add("accountId", accountId.toString().getBytes(StandardCharsets.UTF_8));

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
