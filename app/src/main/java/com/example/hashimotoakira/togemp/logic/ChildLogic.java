package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.List;

// 子が呼ぶロジック
public class ChildLogic {

    public String parentId; // 親のID
    public List<Hand> hands; // 手札

    public ChildLogic(String parentId) {
        this.parentId = parentId;
        this.hands = new ArrayList<>();
    }

    public String getParentId() {
        return parentId;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    // 手札が配られた際に呼ばれる。
    // 手札を作る
    public void createHands(List<Card> cards) {
        int handPosition = 1;
        for (Card card : cards) {
            Hand hand = new Hand(card, handPosition);
            handPosition++;

            this.hands.add(hand);
        }
    }

    // カードを捨てる
    public void discard(int firstHandPosition, int secondHandPosition){

        // 引数で渡されたカード位置で探して、該当するものを削除
        int index = 0;
        for (Hand hand : hands) {
            int handPosition = hand.getHandPosition();
            if ( firstHandPosition == handPosition || secondHandPosition == handPosition){
                hands.remove(index);
            }
        }

        // 手札の位置番号を振り直す
        int newHandPosition = 1;
        for (Hand hand : hands) {
            hand.setHandPosition(newHandPosition);
            newHandPosition++;
        }
    }

    // 現在の手札を取得する
    public List<Hand> getHands() {
        return hands;
    }
    
}
