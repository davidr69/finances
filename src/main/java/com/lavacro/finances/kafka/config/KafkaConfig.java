package com.lavacro.finances.kafka.config;

import com.lavacro.finances.shared.proto.DecisionProto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
	ProducerFactory<String, byte[]> byteArrayProducerFactory(KafkaProperties props) {
        Map<String, Object> config = props.buildProducerProperties();
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    KafkaTemplate<String, byte[]> byteArrayKafkaTemplate(
            ProducerFactory<String, byte[]> byteArrayProducerFactory) {
        return new KafkaTemplate<>(byteArrayProducerFactory);
    }

    @Bean
    ProducerFactory<String, DecisionProto.DecisionMessage> protoProducerFactory(KafkaProperties props) {
        Map<String, Object> config = props.buildProducerProperties();
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DecisionSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    KafkaTemplate<String, DecisionProto.DecisionMessage> protoKafkaTemplate(
            ProducerFactory<String, DecisionProto.DecisionMessage> protoProducerFactory) {
        return new KafkaTemplate<>(protoProducerFactory);
    }
}
