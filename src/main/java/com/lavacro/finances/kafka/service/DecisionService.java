package com.lavacro.finances.kafka.service;

import com.lavacro.finances.shared.proto.DecisionProto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DecisionService {
	private final KafkaTemplate<String, DecisionProto.DecisionMessage> kafkaTemplate;

	public DecisionService(KafkaTemplate<String, DecisionProto.DecisionMessage> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void send(DecisionProto.DecisionMessage model) {
		ProducerRecord<String, DecisionProto.DecisionMessage> rekord = new ProducerRecord<>("finances-decision", null, model);

		kafkaTemplate.send(rekord)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					log.error("Failed to send message: {}", ex.getMessage());
				} else {
					log.info("Sent; partition: {}, offset: {}",
						result.getRecordMetadata().partition(),
						result.getRecordMetadata().offset());
				}
			}
		);
	}
}
