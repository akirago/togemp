package com.example.hashimotoakira.togemp.logic;

public class Card {
    /*
        clover = c
        spade = s
        heart = h
        dia = d
     */
    public  String suit;
    public  int number;

    //  jacksonのライブラリーを使うため空のコンストラクタが必須
    public Card() {
    }

    public Card(String suit, int number) {
        this.suit = suit;
        this.number = number;
    }
}
