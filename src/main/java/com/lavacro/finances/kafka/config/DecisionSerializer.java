package com.lavacro.finances.kafka.config;

import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Serializer;

public class DecisionSerializer<T extends Message> implements Serializer<T> {
	@Override
	public byte[] serialize(String topic, T data) {
		if(data == null) {
			return null;
		}
		return data.toByteArray();
	}
}
