package com.example.hashimotoakira.togemp;

import com.example.hashimotoakira.togemp.logic.Card;
import com.example.hashimotoakira.togemp.logic.ChildLogic;
import com.example.hashimotoakira.togemp.logic.ConnectionMessage;
import com.example.hashimotoakira.togemp.logic.Hand;
import com.example.hashimotoakira.togemp.logic.ParentLogic;
import com.example.hashimotoakira.togemp.logic.PlayerInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NoguchiUnitTest {

    @Test
    public void parentLogicTest() {
        // 親を作る
        System.out.println("\n--------親の作成--------");
        String parentId = "parent";
        ParentLogic parentLogic = new ParentLogic();
        parentLogic.addPlayer(parentId);
        parentLogic.setPlayerPositionById(parentId);

        // 子を作る
        System.out.println("\n--------子の作成--------");
        ArrayList<String> playerIdList = new ArrayList<>();
        playerIdList.add("a");
        playerIdList.add("b");
        playerIdList.add("c");
        for (String playerId : playerIdList) {
            parentLogic.addPlayer(playerId);
            parentLogic.setPlayerPositionById(playerId);
        }

        // 各プレイヤーの手札を作る
        parentLogic.createHands();

        System.out.println("\n--------各プレイヤーの手札作成--------");
        for (PlayerInfo info : parentLogic.playerInfoList) {
            String id = null;
            List<Card> initialHands = new ArrayList<>();
            for (PlayerInfo playerInfo : parentLogic.playerInfoList) {
                int position = info.getPosition();
                if (position == playerInfo.getPosition()) {
                    id = playerInfo.getId();
                    initialHands = playerInfo.getInitialHands();
                    break;
                }
            }
            for (Card hand : initialHands) {
                System.out.println("プレイヤーID: " + id + " カード: " + hand.suit + " " + hand.number + " カード枚数" + info.getCardCount());
            }
        }

        System.out.println("\n--------自分がparentだとしてプレイ開始--------");
        PlayerInfo playerMyself = parentLogic.playerInfoList.stream().filter(playerInfo -> playerInfo.getId().equals(parentId)).findFirst().get();
        List<Card> myInitialHands = playerMyself.getInitialHands();
        ChildLogic childLogic = new ChildLogic();
        childLogic.createHands(myInitialHands);
        System.out.println("\n--------自分の手札--------");
        childLogic.getHands().forEach(myHand -> {
            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
        });

        System.out.println("\n--------一枚引く--------");
        Card testCard = new Card("t", 1);
        ArrayList<Card> testCardList = new ArrayList<>();
        testCardList.add(testCard);
        childLogic.receiveCard(testCardList);
        childLogic.getHands().forEach(myHand -> {
            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
        });

        System.out.println("\n--------一枚渡す--------");
        List<Card> cards = childLogic.sendCard(childLogic.getHands().size());
        childLogic.getHands().forEach(myHand -> {
            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
        });


        System.out.println("\n--------二枚捨てる--------");
        childLogic.discard(1, 2);
        childLogic.getHands().forEach(myHand -> {
            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
        });

        System.out.println("\n--------カードの順番を入れ替える--------");
        childLogic.changeHandsPosition();
//        childLogic.getHands().forEach(myHand -> {
//            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
//        });
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });
    }
}
