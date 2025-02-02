package com.zakobura.together.togemp;

import com.zakobura.together.togemp.logic.Card;
import com.zakobura.together.togemp.logic.ConnectionMessage;
import com.zakobura.together.togemp.logic.ParentLogic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        ConnectionMessage hoge = new ConnectionMessage(ConnectionMessage.ReceiverAction.DealCard, Arrays.asList(new Card("sute", 2)));
        System.out.print(hoge.getReceiverAction());
        String fuga = ConnectionMessage.createStrMsg(ConnectionMessage.ReceiverAction.GetCard,  Arrays.asList(new Card("sute", 2)));
        System.out.print(fuga);
        ConnectionMessage after = ConnectionMessage.parseStrMsg(fuga);
        System.out.print(after.getReceiverAction());

//        String finishMsg = ConnectionMessage.createStrFinishMsg();
//        ConnectionMessage finishObj = ConnectionMessage.parseStrMsg(finishMsg);
//        System.out.print(finishObj.getReceiverAction());

        String rankMsg = ConnectionMessage.createStrRankMsg(3);
        ConnectionMessage rankObj = ConnectionMessage.parseStrMsg(rankMsg);
        System.out.print(rankObj.rank);

    }


    @Test
    public void addition_changeToNextUser() throws Exception {
        // 親を作る
        String parentId = "parent";
        ParentLogic parentLogic = new ParentLogic();
        parentLogic.addPlayer(parentId);
        parentLogic.setPlayerPositionById(parentId);
        // 親の手持ちは4まい
        parentLogic.updatePlayerHandsCount(parentId, 4);


        // 子を作る
        ArrayList<String> playerIdList = new ArrayList<>();
        playerIdList.add("a");
        playerIdList.add("b");
        playerIdList.add("c");
        for (String playerId : playerIdList) {
            parentLogic.addPlayer(playerId);
            parentLogic.setPlayerPositionById(playerId);
            // 子供の手持ちは3まい
            parentLogic.updatePlayerHandsCount(playerId, 3);
        }
        // 残り枚数を確認
        parentLogic.getPlayerInfoList().forEach(player -> {
                    System.out.print(player.getCardCount());
                }
        );
        // 最初は親
        System.out.print(parentLogic.getSendPlayer().getId());
        parentLogic.changeToNextTurn();
        // 次はa
        System.out.print(parentLogic.getSendPlayer().getId());
        parentLogic.changeToNextTurn();
        // 次はb
        System.out.print(parentLogic.getSendPlayer().getId());
        parentLogic.changeToNextTurn();
        parentLogic.changeToNextTurn();
        // また親に戻って来る
        System.out.print(parentLogic.getSendPlayer().getId());

        // aが0まいで終了
        parentLogic.updatePlayerHandsCount("a", 0);
        // 親の次がbになっている
        parentLogic.changeToNextTurn();
        System.out.print(parentLogic.getSendPlayer().getId());
    }
}