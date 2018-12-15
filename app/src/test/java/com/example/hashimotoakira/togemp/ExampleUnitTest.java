package com.example.hashimotoakira.togemp;

import android.util.Log;

import com.example.hashimotoakira.togemp.logic.Card;
import com.example.hashimotoakira.togemp.logic.ConnectionMessage;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        ConnectionMessage hoge = new ConnectionMessage(ConnectionMessage.ReceiverAction.TransferCard, Arrays.asList(new Card("sute", 2)));
        System.out.print(hoge.getReceiverAction());
        String fuga = ConnectionMessage.createStrMsg(ConnectionMessage.ReceiverAction.GetCard,  Arrays.asList(new Card("sute", 2)));
        System.out.print(fuga);
        ConnectionMessage after = ConnectionMessage.parseStrMsg(fuga);
        System.out.print(after.getReceiverAction());
    }
}