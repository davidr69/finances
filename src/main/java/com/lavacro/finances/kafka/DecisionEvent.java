package com.lavacro.finances.kafka;

import com.lavacro.finances.shared.proto.DecisionProto;

public record DecisionEvent(byte[] payload) {
	public static DecisionEvent of(DecisionProto.DecisionMessage message) {
		return new DecisionEvent(message.toByteArray());
	}
}
