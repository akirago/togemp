package com.example.hashimotoakira.togemp.logic;

public class Hand {
    private Card card;
    private int handPosition;

    public Hand(Card card, int handPosition) {
        this.card = card;
        this.handPosition = handPosition;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public int getHandPosition() {
        return handPosition;
    }

    public void setHandPosition(int handPosition) {
        this.handPosition = handPosition;
    }

}
