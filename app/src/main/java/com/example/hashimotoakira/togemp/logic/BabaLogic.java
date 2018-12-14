package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.List;

public class BabaLogic {
    List<Player> playerList;

    public BabaLogic() {
        this.playerList = new ArrayList<>();
    }

    public void addPlayer(String id) {
        playerList.add(new Player(id));
    }

    public void addPlayerPosition(String id, int position) {
        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            if (id.equals(player.getId())){
                player.setPosition(position);
            }
        }
    }
}
