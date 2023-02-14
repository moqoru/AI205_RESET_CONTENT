package com.project.arc205.game.gamedata.event.handler;

import com.project.arc205.common.dto.BaseResponse;
import com.project.arc205.common.operation.operation.CharacterOperation;
import com.project.arc205.common.service.PlayerRoomMappingRepository;
import com.project.arc205.common.service.PlayerSessionMappingService;
import com.project.arc205.common.util.Constant;
import com.project.arc205.common.util.WebSocketUtil;
import com.project.arc205.game.gamecharacter.model.entity.GameCharacter;
import com.project.arc205.game.gamecharacter.model.entity.Mafia;
import com.project.arc205.game.gamedata.event.SabotageCloseEvent;
import com.project.arc205.game.gamedata.event.SabotageOpenEvent;
import com.project.arc205.game.gamedata.event.SabotageRequestEvent;
import com.project.arc205.game.gamedata.model.entity.GameData;
import com.project.arc205.game.gamedata.model.entity.Sabotage;
import com.project.arc205.game.gamedata.repository.GameRepository;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SabotageEventHandler {

    private final GameRepository gameRepository;
    private final PlayerSessionMappingService mappingService;
    private final PlayerRoomMappingRepository playerRoomMappingRepository;
    private final SimpMessagingTemplate template;
    private final TaskScheduler taskScheduler;

    @Async
    @EventListener
    public void onSabotageRequest(SabotageRequestEvent event) {
        log.info("on sabotage request : {}", event);
        UUID roomId = playerRoomMappingRepository.findRoomIdByPlayerId(event.getPlayerId());
        GameData gameData = gameRepository.findById(roomId);
        Sabotage sabotage = gameData.getSabotage();
        sabotage.open();
    }

    @Async
    @EventListener
    public void onSabotageOpen(SabotageOpenEvent event) {
        log.info("on sabotage open : {}", event);
        String destination = Constant.DESTINATION_PREFIX + event.getRoomId();

        template.convertAndSend(destination,
                BaseResponse.character(CharacterOperation.SABOTAGE_OPEN).build());
    }

    @Async
    @EventListener
    public void onSabotageClose(SabotageCloseEvent event) {
        log.info("on sabotage close : {}", event);

        UUID roomId = event.getRoomId();
        GameData gameData = gameRepository.findById(roomId);

        sendMessageTo(roomId, gameData.getGameCharacters());

        Sabotage sabotage = gameData.getSabotage();

        taskScheduler.schedule(() -> sabotage.setCoolTime(false),
                new Date(System.currentTimeMillis() + sabotage.getCoolTime()));

    }

    private void sendMessageTo(UUID roomId, Map<String, GameCharacter> gameCharacters) {
        for (GameCharacter gameCharacter : gameCharacters.values()) {
            if (gameCharacter instanceof Mafia) {
                continue;
            }

            String sessionIdInRoom = mappingService.convertPlayerIdToSessionIdInRoom(roomId,
                    gameCharacter.getPlayerId());

            template.convertAndSendToUser(sessionIdInRoom, "/queue",
                    BaseResponse.character(CharacterOperation.SABOTAGE_CLOSE).build(),
                    WebSocketUtil.createHeaders(sessionIdInRoom));
        }
    }
}
