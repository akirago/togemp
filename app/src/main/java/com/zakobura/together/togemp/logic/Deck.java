package com.zakobura.together.togemp.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    public List<Card> cardList;

    // 初期化で52枚カードとジョーカを生成してシャッフル
    public Deck() {
        this.cardList = new LinkedList<>();
        List<String> suitList = Arrays.asList("s", "h", "d", "c");
        for (String suit : suitList) {
            for (int i = 1; i <= 13; i++) {
                cardList.add(new Card(suit, i));
            }
        }
        Card joker = new Card("joker",0);
        this.cardList.add(joker);
        Collections.shuffle(cardList);
    }

    public Deck(int cardMaxNumber) {
        this.cardList = new LinkedList<>();
        List<String> suitList = Arrays.asList("s", "h", "d", "c");
        for (String suit : suitList) {
            for (int i = 1; i <= cardMaxNumber; i++) {
                cardList.add(new Card(suit, i));
            }
        }
        Card joker = new Card("joker",0);
        this.cardList.add(joker);
        Collections.shuffle(cardList);
    }

    public boolean isEmpty(){
        return cardList.isEmpty();
    }

    // デッキからカードをドローする
    public Card draw(){
        int lastIndexOfCardList = cardList.size() - 1;
        Card drawnCard = cardList.get(lastIndexOfCardList);
        cardList.remove(lastIndexOfCardList);
        return drawnCard;
    }

//    public  Optional<Card> getCard {
//        if (this.cardList.size() == 0) {
//            return Optional.empty();
//        }
//        Card card = this.cardList.get(0);
//        this.cardList.remove(0);
//        return Optional.of(card);
//    }
}