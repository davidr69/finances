package com.lavacro.finances.kafka.service;

import com.lavacro.finances.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NotifyAgent {
	private final KafkaTemplate<String, byte[]> kafkaTemplate;

	public NotifyAgent(KafkaTemplate<String, byte[]> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public CompletableFuture<GenericResponse> send(String filename, Integer accountId, Integer year, byte[] content) {
		ProducerRecord<String, byte[]> rekord = new ProducerRecord<>("finances-topic", null, content);
		rekord.headers()
			.add("filename", filename.getBytes(StandardCharsets.UTF_8))
			.add("accountId", accountId.toString().getBytes(StandardCharsets.UTF_8))
			.add("year", year.toString().getBytes(StandardCharsets.UTF_8));

		return kafkaTemplate.send(rekord).handle((result, ex) -> {
			GenericResponse resp = new GenericResponse();
			if (ex != null) {
				log.error("Failed to send message: {}", ex.getMessage());
				resp.setCode(1);
				resp.setMessage(ex.getMessage());
			} else {
				log.info("Sent; partition: {}, offset: {}",
					result.getRecordMetadata().partition(),
					result.getRecordMetadata().offset());
				resp.setCode(0);
				resp.setMessage("Message sent successfully");
			}
			return resp;
		});
	}
}
