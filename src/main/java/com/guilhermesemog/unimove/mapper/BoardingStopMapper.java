package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopCreate;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.model.BoardingStop;
import org.springframework.stereotype.Component;

@Component
public class BoardingStopMapper {

    public BoardingStop toEntity(BoardingStopCreate body) {
        return new BoardingStop(
                body.local()
        );
    }

    public BoardingStopResponse toResponse(BoardingStop bs) {
        return new BoardingStopResponse(
                bs.getId(),
                bs.getLocal()
        );
    }
}
