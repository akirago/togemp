package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


// 親が呼ぶよてい
public class ParentLogic {
    public List<PlayerInfo> playerInfoList;

    public ParentLogic() {
        this.playerInfoList = new ArrayList<>();
    }

    public void addPlayer(String id) {
        playerInfoList.add(new PlayerInfo(id));
    }

    public void setPlayerPositionById(String id) {
        Optional<Integer> optMaxPosition = playerInfoList.stream().map(playerInfo -> playerInfo.getPosition()).max(Comparator.naturalOrder());
        Integer maxPosition = optMaxPosition.orElse(0);

        playerInfoList.stream().filter(playerInfo -> id.equals(playerInfo.getId())).forEach(playerInfo -> {
            playerInfo.setPosition(maxPosition + 1);
        });
    }

    public List<Card> setParentCard() {

        return null; // cardのはいれつ
    }
}
