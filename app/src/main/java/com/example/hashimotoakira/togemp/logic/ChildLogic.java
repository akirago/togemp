package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 子が呼ぶロジック
public class ChildLogic {

    public String parentId; // 親のID
    public List<Hand> hands; // 手札

    public ChildLogic() {
        this.hands = new ArrayList<>();
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    // 現在の表示するカード一覧を取得する。表示する順番にソートされている。
    public List<Card> getSortCardList() {
        return hands.stream().sorted(Comparator.comparing(Hand::getHandPosition))
                .map(hand -> hand.getCard()).collect(Collectors.toList());
    }

    // 現在の手札枚数を取得する
    public int getHandsCount(){
        return hands.size();
    }

    // カードを引いて、手札に加える
    public void receiveCard(List<Card> cards){
        Integer maxHandPosition = hands.stream().map(hand -> hand.getHandPosition()).max(Comparator.naturalOrder()).orElse(0);
        Hand hand = new Hand(cards.get(0), maxHandPosition + 1);
        hands.add(hand);
    }

    // 手札が引かれた際に呼ばれる
    // 手札を一枚捨てて、捨てたカード（渡したカード）を返す
    public List<Card> sendCard(int targetHandPosition){
        int index = 0;
        Card card = null;
        for (Hand hand : hands) {
            int handPosition = hand.getHandPosition();
            if ( targetHandPosition == handPosition){
                card = hand.getCard();
                hands.remove(index);
                break;
            }
        }

        // 手札の位置番号を振り直す
        int newHandPosition = 1;
        for (Hand hand : hands) {
            hand.setHandPosition(newHandPosition);
            newHandPosition++;
        }
        List<Card> cards = new ArrayList<>();
        cards.add(card);
        return cards;
    }
}
