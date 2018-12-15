package com.example.hashimotoakira.togemp.logic;

import java.util.List;

public class ChildLogic {

    public String parentId;
    public List<Hand> hands;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    public List<Hand> getHands() {
        return hands;
    }

    public String sendCard(int cardPosition){
        return null;
    }

}
