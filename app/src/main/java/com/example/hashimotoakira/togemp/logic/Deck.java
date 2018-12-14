package com.example.hashimotoakira.togemp.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    public List<Card> deck;

    // 初期化で52枚カードとジョーカを生成してシャッフル
    public Deck() {
        this.deck = new LinkedList<>();
        List<String> suitList = Arrays.asList("spade", "hart", "dia", "clover");
        for (String suit : suitList) {
            for (int i = 1; i <= 13; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Card joker = new Card("joker",0);
        this.deck.add(joker);
        Collections.shuffle(deck);
    }

//    public  Optional<Card> getCard {
//        if (this.deck.size() == 0) {
//            return Optional.empty();
//        }
//        Card card = this.deck.get(0);
//        this.deck.remove(0);
//        return Optional.of(card);
//    }
}