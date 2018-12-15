package com.example.hashimotoakira.togemp.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 子が呼ぶロジック
 */
public class ChildLogic {

    private String parentId; // 親のID
    private List<Hand> hands; // 手札

    public ChildLogic() {
        this.hands = new ArrayList<>();
    }

    /**
     * 親のIDをセットする
     * @param parentId 親のID
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 親のIDを取得する
     * @return 親のID
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 手札が作られた際に呼ばれ、初期手札を生成する
     * @param cards 初期手札のリスト
     */
    public void createHands(List<Card> cards) {
        int handPosition = 1;
        for (Card card : cards) {
            Hand hand = new Hand(card, handPosition);
            handPosition++;

            this.hands.add(hand);
        }
    }

    /**
     * カードを二枚受け取り捨てる
     * @param firstHandPosition 一枚目のカードの位置
     * @param secondHandPosition 二枚目のカードの位置
     * @return カードを捨てることに成功したかどうか
     */
    public boolean discard(int firstHandPosition, int secondHandPosition){
        if (firstHandPosition == secondHandPosition) {
            return false;
        }

        Hand firstHand = hands.stream().filter(hand -> firstHandPosition == hand.getHandPosition()).findFirst().get();
        Hand secondHand = hands.stream().filter(hand -> secondHandPosition == hand.getHandPosition()).findFirst().get();

        if (firstHand.getCard().number != secondHand.getCard().number){
            return false;
        }

        // 引数で渡されたカード位置で探して、該当するものを削除
        for (int i = 0; i < hands.size(); i++) {
            Hand hand = hands.get(i);
            int handPosition = hand.getHandPosition();
            if ( firstHandPosition == handPosition){
                hands.remove(i);
            }
        }

        for (int i = 0; i < hands.size(); i++) {
            Hand hand = hands.get(i);
            int handPosition = hand.getHandPosition();
            if ( secondHandPosition == handPosition){
                hands.remove(i);
            }
        }

        // 手札の位置番号を振り直す
        int newHandPosition = 1;
        for (Hand hand : hands) {
            hand.setHandPosition(newHandPosition);
            newHandPosition++;
        }

        return true;
    }

    /**
     * 現在の表示するカード一覧を取得する。表示する順番にソートされている。
     * @return 現在の表示するカード一覧
     */
    public List<Card> getSortCardList() {
        return hands.stream().sorted(Comparator.comparing(Hand::getHandPosition))
                .map(hand -> hand.getCard()).collect(Collectors.toList());
    }

    /**
     * 現在の手札枚数を取得する
     * @return 現在の手札枚数
     */
    public int getCardsCount(){
        return hands.size();
    }

    /**
     * カードを引いて、手札に加える
     * @param cards 引いたカードを含んだカードのリスト ※一枚だけ渡ってくる想定、メッセージのパースの関係でリストで渡してもらう
     */
    public void receiveCard(List<Card> cards){
        Integer maxHandPosition = hands.stream().map(hand -> hand.getHandPosition()).max(Comparator.naturalOrder()).orElse(0);
        Hand hand = new Hand(cards.get(0), maxHandPosition + 1);
        hands.add(hand);
    }

    /**
     * 手札が引かれた際に呼ばれる
     * 手札を一枚捨てて、捨てたカード（渡したカード）を返す
     * @param targetHandPosition 引かれたカードの位置
     * @return 捨てたカード
     */
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
            index++;
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

    /**
     * 手札の位置をシャッフルする
     */
    public void shuffleCards(){
        int handsSize = hands.size();
        List<Integer> positionList = new ArrayList<>();
        for (int i = 1; i <= handsSize; i++) {
            positionList.add(i);
        }

        Collections.shuffle(positionList);

        for (int i = 0; i < handsSize; i++) {
            Hand hand = hands.get(i);
            hand.setHandPosition(positionList.get(i));
        }
    }
}
