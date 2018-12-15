package com.example.hashimotoakira.togemp.logic;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.hashimotoakira.togemp.util.LOGKt.logD;


// 親が呼ぶロジック
public class ParentLogic {

    public static final String PARENT_ID = "parent";

    public List<PlayerInfo> playerInfoList;

    // プレイヤーの参加人数を返す
    public int getPlayerInfoCount() {
        return playerInfoList.size();
    }

    public ParentLogic() {
        this.playerInfoList = new ArrayList<>();
    }

    // プレイヤー情報を追加する
    public void addPlayer(String id) {
        playerInfoList.add(new PlayerInfo(id));
    }

    // プレイヤーの順番を設定する
    public void setPlayerPositionById(String id) {
        Optional<Integer> optMaxPosition = playerInfoList.stream().map(playerInfo -> playerInfo.getPosition()).max(Comparator.naturalOrder());
        Integer maxPosition = optMaxPosition.orElse(0);
        playerInfoList.stream().filter(playerInfo -> id.equals(playerInfo.getId())).forEach(playerInfo -> {
            // 親が最初にカードを渡すようにする
            if (maxPosition.equals(0)) {
                playerInfo.setSendCardPlayer(true);
            }
            playerInfo.setPosition(maxPosition + 1);
        });
    }

    // 新しくデッキを作成する
    public Deck createDeck() {
        return new Deck();
    }

    // 新しく各プレイヤーに対し手札を作成する
    public void createHands() {
        Deck deck = createDeck();
        for (PlayerInfo playerInfo : playerInfoList) {
            playerInfo.createEmptyHands();
        }

        // デッキがなくなるまで配り続ける
        while (!deck.isEmpty()) {
            // プレイヤーに等分して配る
            for (PlayerInfo playerInfo : playerInfoList) {
                if (deck.isEmpty()) {
                    break;
                }
                playerInfo.draw(deck);
            }
        }
        logD("dealButton  end");
    }

    // 各プレイヤーの順番を引数で受け取り、そのプレイヤーの初期手札を返す
    public Pair<String, List<Card>> getPlayerInitialHands(int position) {
        String id = null;
        List<Card> initialHands = new ArrayList<>();
        for (PlayerInfo playerInfo : playerInfoList) {
            if (position == playerInfo.getPosition()) {
                id = playerInfo.getId();
                initialHands = playerInfo.getInitialHands();
                break;
            }
        }
        return new Pair<>(id, initialHands);
    }

    // プレイヤーの手札枚数を更新する
    public void updatePlayerHandsCount(String id, int cardCount) {
        PlayerInfo playerInfo = playerInfoList.stream().filter(info -> id.equals(info.getId())).findFirst().get();
        playerInfo.setCardCount(cardCount);
    }

    // 次のターンに切り替える
    public void changeToNextTurn() {
        PlayerInfo currentPlayer = getSendPlayer();
        PlayerInfo nextPlayer = getRecievePlayer();
        currentPlayer.setSendCardPlayer(false);
        nextPlayer.setSendCardPlayer(true);
    }

    // カードを送信するプレイヤーを取得する
    public PlayerInfo getSendPlayer() {
        return playerInfoList.stream().filter(playerInfo -> playerInfo.isSendCardPlayer()).findFirst().get();
    }

    // カードを受け取りプレイヤーを取得する.存在しない場合はnullが返る
    public PlayerInfo getRecievePlayer() {
        // ポジションごとに並び替え
        List<PlayerInfo> restPlayerList = playerInfoList.stream().filter(playerInfo -> playerInfo.getCardCount() != 0).sorted(Comparator.comparing(PlayerInfo::getPosition)).collect(Collectors.toList());
        if (restPlayerList.isEmpty()) {
            return null;
        }
        // 最後の場合は、リストの最初のプレイヤー次のプレイヤー
        if (restPlayerList.get(restPlayerList.size() - 1).isSendCardPlayer()) {
            return restPlayerList.get(0);
        }
        // それ以外はsendPlayerの次のポジションのplayer
        for (int i = 0; i < restPlayerList.size(); i++) {
            if (restPlayerList.get(i).isSendCardPlayer()) {
                return restPlayerList.get(i + 1);
            }
        }
        // ここまで来るロジックはないけど、エディタで警告が出るのでnullを返す
        return null;
    }
}
