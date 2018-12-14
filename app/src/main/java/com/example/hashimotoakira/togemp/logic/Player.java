package com.example.hashimotoakira.togemp.logic;

public class Player {

    public void setPosition(int position) {
        this.position = position;
    }

    public int position;

    public String id;

    public int cardCount;

    Player(String id) {
        this.id = id;
    }
}
