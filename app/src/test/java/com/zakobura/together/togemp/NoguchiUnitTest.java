package com.zakobura.together.togemp;

import com.zakobura.together.togemp.logic.Card;
import com.zakobura.together.togemp.logic.ChildLogic;
import com.zakobura.together.togemp.logic.ParentLogic;
import com.zakobura.together.togemp.logic.PlayerInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        for (PlayerInfo info : parentLogic.getPlayerInfoList()) {
            String id = null;
            List<Card> initialHands = new ArrayList<>();
            for (PlayerInfo playerInfo : parentLogic.getPlayerInfoList()) {
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
        PlayerInfo playerMyself = parentLogic.getPlayerInfoList().stream().filter(playerInfo -> playerInfo.getId().equals(parentId)).findFirst().get();
        List<Card> myInitialHands = playerMyself.getInitialHands();
        ChildLogic childLogic = new ChildLogic();
        childLogic.createHands(myInitialHands);
        System.out.println("\n--------自分の手札--------");
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });

        System.out.println("\n--------一枚引く--------");
        Card testCard = new Card("t", 1);
        ArrayList<Card> testCardList = new ArrayList<>();
        testCardList.add(testCard);
        childLogic.receiveCard(testCardList);
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });

        System.out.println("\n--------一枚渡す--------");
        List<Card> cards = childLogic.sendCard(childLogic.getSortCardList().size());
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });


        System.out.println("\n--------二枚捨てる--------");
        childLogic.receiveCard(Arrays.asList(new Card("t", 1)));
        childLogic.receiveCard(Arrays.asList(new Card("t", 1)));
        System.out.println("\n--------捨てる前--------");
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });
        boolean discarded = childLogic.discard(childLogic.getSortCardList().size() - 1, childLogic.getSortCardList().size());
        System.out.println("\n--------捨てたあと--------");
        System.out.println("成功?: " + discarded);
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });

        System.out.println("\n--------カードの順番を入れ替える--------");
        childLogic.shuffleCards();
//        childLogic.getHands().forEach(myHand -> {
//            System.out.println( "カード マーク: " + myHand.getCard().suit + " 数字: " + myHand.getCard().number + " 順番: " + myHand.getHandPosition());
//        });
        childLogic.getSortCardList().forEach(card -> {
            System.out.println( "カード マーク: " + card.suit + " 数字: " + card.number);
        });

        System.out.println("\n--------順位をつけていく--------");
        for (int i = 0; i < parentLogic.getPlayerInfoList().size(); i++) {
            PlayerInfo playerInfo = parentLogic.getPlayerInfoList().get(i);
            int rank = parentLogic.finishPlaying(playerInfo.getId());
            System.out.println("プレイヤーID: " + playerInfo.getId() + " 順位: " + rank);
        }
    }
}
