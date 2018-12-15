package com.example.hashimotoakira.togemp.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;

public class ConnectionMessage {
    public ReceiverAction receiverAction;
    public List<Card> cardList;

    //  jacksonのライブラリーを使うため空のコンストラクタが必須
    public ConnectionMessage() {
    }

    public ConnectionMessage(ReceiverAction receiverAction) {
        this.receiverAction = receiverAction;
    }

    public ConnectionMessage(ReceiverAction receiverAction, List<Card> cardList) {
        this.receiverAction = receiverAction;
        this.cardList = cardList;
    }

    public ReceiverAction getReceiverAction() {
        return receiverAction;
    }

    public List<Card> getCardList() {
        return cardList;
    }

    public void setActionType(ReceiverAction receiverAction) {
        this.receiverAction = receiverAction;
    }

    public void setCardList(List<Card> cardList) {
        this.cardList = cardList;
    }

    public enum ReceiverAction
    {
        GetCard,
        DealCard,
        DiscardFinish,
        DrawFinish,
        YourTurn
    }

    public static String createStrMsg(ReceiverAction receiverAction, List<Card> cardList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionMessage msg = new ConnectionMessage(receiverAction, cardList);
        return mapper.writeValueAsString(msg);
    }

    public static String createStrCardMsg(ReceiverAction receiverAction, List<Card> cardList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionMessage msg = new ConnectionMessage(receiverAction, cardList);
        return mapper.writeValueAsString(msg);
    }

    public static String createStrDiscardFinishMsg() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionMessage msg = new ConnectionMessage(ReceiverAction.DiscardFinish);
        return mapper.writeValueAsString(msg);
    }

    public static String createStrDrawFinishMsg() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionMessage msg = new ConnectionMessage(ReceiverAction.DrawFinish);
        return mapper.writeValueAsString(msg);
    }

    public static String createStrYourTurnMsg() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConnectionMessage msg = new ConnectionMessage(ReceiverAction.YourTurn);
        return mapper.writeValueAsString(msg);
    }
    
    public static ConnectionMessage parseStrMsg(String jsonStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, ConnectionMessage.class);
    }
}
