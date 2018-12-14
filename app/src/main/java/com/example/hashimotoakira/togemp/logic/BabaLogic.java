package com.example.hashimotoakira.togemp.logic;

import java.util.List;

public class BabaLogic {
    List<Player> playerList;

    public void addPlayer(String id) {
        playerList.add(new Player(id));
    }
}
