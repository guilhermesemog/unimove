package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopPostRequestBody;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponseBody;
import com.guilhermesemog.unimove.model.BoardingStop;
import org.springframework.stereotype.Component;

@Component
public class BoardingStopMapper {

    public BoardingStop toEntity(BoardingStopPostRequestBody boardingStopPostRequestBody) {
        return new BoardingStop(
                boardingStopPostRequestBody.local()
        );
    }

    public BoardingStopResponseBody toResponseBody(BoardingStop boardingStop) {
        return new BoardingStopResponseBody(
                boardingStop.getId(),
                boardingStop.getLocal()
        );
    }
}
