package com.project.arc205.game.meeting.dto.response;

import com.project.arc205.common.dto.BaseResponse;
import com.project.arc205.common.operation.Type;
import com.project.arc205.common.operation.operation.MeetingOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class VotedResponse {

    private final Player player;
    private final int remainingVoteTicket;
    private VotedResponse(String playerId, int remainingVoteTicket) {
        this.player = Player.of(playerId);
        this.remainingVoteTicket = remainingVoteTicket;
    }

    static public BaseResponse<VotedResponse> newBaseResponse(String playerId,
            int remainingVoteTicket) {
        return BaseResponse.of(Type.MEETING, MeetingOperation.VOTE,
                new VotedResponse(playerId, remainingVoteTicket));
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    private static class Player {

        String id;
    }
}
