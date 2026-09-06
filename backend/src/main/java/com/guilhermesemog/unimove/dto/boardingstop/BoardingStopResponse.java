package com.guilhermesemog.unimove.dto.boardingstop;

import java.util.UUID;
public record BoardingStopResponse(
        UUID id,
        String local
) {
}
