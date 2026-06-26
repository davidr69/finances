package com.lavacro.finances.kafka.config;

import com.lavacro.finances.kafka.model.DecisionModel;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

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
    ProducerFactory<String, DecisionModel> jsonProducerFactory(KafkaProperties props) {
        Map<String, Object> config = props.buildProducerProperties();
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    KafkaTemplate<String, DecisionModel> jsonKafkaTemplate(
            ProducerFactory<String, DecisionModel> jsonProducerFactory) {
        return new KafkaTemplate<>(jsonProducerFactory);
    }
}
