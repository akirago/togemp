package com.example.hashimotoakira.togemp;

import com.example.hashimotoakira.togemp.logic.Card;
import com.example.hashimotoakira.togemp.logic.ConnectionMessage;
import com.example.hashimotoakira.togemp.logic.ParentLogic;
import com.example.hashimotoakira.togemp.logic.PlayerInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoguchiUnitTest {
    
    @Test
    public void parentLogicTest() {
        // 親を作る
        String parentId = "parent";
        ParentLogic parentLogic = new ParentLogic();
        parentLogic.addPlayer(parentId);
        parentLogic.setPlayerPositionById(parentId);

        // 子を作る
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
                System.out.println("プレイヤーID: " + info.getId() + " カード: " + hand.suit + " " + hand.number + " カード枚数" + info.getCardCount());
            }
        }
    }
}
