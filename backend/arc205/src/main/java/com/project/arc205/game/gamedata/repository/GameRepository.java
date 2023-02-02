package com.project.arc205.game.gamedata.repository;

import com.project.arc205.common.Constant;
import com.project.arc205.common.model.Location;
import com.project.arc205.game.gamecharacter.model.entity.Citizen;
import com.project.arc205.game.gamecharacter.model.entity.GameCharacter;
import com.project.arc205.game.gamecharacter.model.entity.Mafia;
import com.project.arc205.game.gamedata.model.entity.GameData;
import com.project.arc205.game.gamedata.model.exception.GameNotFoundException;
import com.project.arc205.game.room.model.entity.Room;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class GameRepository {

    private final ConcurrentHashMap<UUID, GameData> gameStorage = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Map<String, GameCharacter> gameCharacters = new HashMap<>();
        Location loc = new Location(0.0, 0.0);
        gameCharacters.put("p1", new Citizen(loc, null));
        gameCharacters.put("p2", new Citizen(loc, null));
        gameCharacters.put("p3", new Mafia(loc, null));

        GameData tmpGame = new GameData(5, 2, 1, 3, 30,
                gameCharacters);

        gameStorage.put(UUID.fromString(Constant.TEMP_ROOM_ID), tmpGame);
    }

    public GameData create(Room room) {
        GameData game = GameData.of(room);
        gameStorage.put(room.getId(), game);
        return game;
    }

    public GameData findById(UUID id) {
        if (!gameStorage.containsKey(id)) {
            throw new GameNotFoundException(id.toString());
        }
        return gameStorage.get(id);
    }

}