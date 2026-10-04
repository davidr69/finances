package com.lavacro.finances.kafka;

import com.lavacro.finances.shared.proto.DecisionProto;

public record DecisionEvent(
	DecisionProto.DecisionMessage message
) {
}
