package com.example.hashimotoakira.togemp.logic;

public class Player {

    public String id;
    public int position;
    public int cardCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }


    Player(String id) {
        this.id = id;
    }
}
