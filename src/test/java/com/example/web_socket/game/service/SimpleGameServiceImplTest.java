package com.example.web_socket.game.service;

import com.example.web_socket.domain.Board;
import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.Player;
import com.example.web_socket.service.SimpleGameServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleGameServiceImplTest {

    SimpleGameServiceImpl gameService;
    List<Player> players = new ArrayList<>();

    @BeforeEach
    void setUp() {
        gameService = new SimpleGameServiceImpl();
        gameService.addPlayer("session1");
        gameService.addPlayer("session2");

        players.add(gameService.getPlayer("session1"));
        players.add(gameService.getPlayer("session2"));
    }

    @Test
    void isGameOverTrue() {
        //given
        Player p = gameService.getPlayer(players.get(0).getId());
        Board b = gameService.getBoard(2);
        b.setOwner(gameService.getPlayer(players.get(1).getId()));
        b.setPrice(p.getMoney() + 1000);

        //when
        boolean res = gameService.isGameOver(p.getId(), b.getId());

        //then
        assertThat(res).isTrue();
    }

    @Test
    void isGameOverFalse() {
        //given
        Player p = gameService.getPlayer(players.get(0).getId());
        Board b = gameService.getBoard(2);
        b.setOwner(gameService.getPlayer(players.get(1).getId()));
        b.setPrice(p.getMoney() - 1000);

        //when
        boolean res1 = gameService.isGameOver(p.getId(), b.getId());
        boolean res2 = gameService.isGameOver(p.getId(), 1);

        //then
        assertThat(res1).isFalse();
        assertThat(res2).isFalse();
    }

    @Test
    void setRoll() {
        boolean res = false;
        for (int i=0; i<10000; i++) {
            int roll = gameService.setRoll(players.get(0).getId());
            if (!(1 <= roll && roll <= 6)) res = true;
        }

        assertThat(res).isFalse();
        assertThat(players.get(0).getRoll()).isNotEqualTo(0);
    }

    @Test
    void buySuccess() {
        //given
        Player p = gameService.getPlayer(players.get(0).getId());
        Board b = gameService.getBoard(2);
        b.setPrice(p.getMoney() - 1000);

        //when
        boolean res = gameService.buy(p.getId(), b.getId());

        //then
        assertThat(res).isTrue();
        assertThat(p.getMoney()).isEqualTo(1000);
    }

    @Test
    void buyFail() {
        //given
        Player p = gameService.getPlayer(players.get(0).getId());
        Board b = gameService.getBoard(2);
        int prevMoney = p.getMoney();
        b.setPrice(p.getMoney() + 1000);

        //when
        boolean res = gameService.buy(p.getId(), b.getId());

        //then
        assertThat(res).isFalse();
        assertThat(p.getMoney()).isEqualTo(prevMoney);
    }

    @Test
    @DisplayName("플레이어가 소유주 일때(미구현)")
    void process() {
        //given

        //when

        //then
    }

    @Test
    @DisplayName("플레이어가 소유주가 아닐때 + 비어있는 땅일 경우(강제 구입 버전)")
    void process1() {
        //given
        Player p1 = gameService.getPlayer(players.get(0).getId());
        int prevMoney = p1.getMoney();
        Player p2 = gameService.getPlayer(players.get(1).getId());
        int prevMoney2 = p2.getMoney();

        Board currentB = gameService.getBoard(2);
        currentB.getOnPlayers().add(p1); // p1 을 currentB에 위치시킴

        int roll = gameService.setRoll(p1.getId());
        Board newB = gameService.getBoard(currentB.getId() + roll);
        newB.setPrice(p1.getMoney() - 1000);
        newB.setOwner(p2);

        //p1의 돈은 1000이어야하고
        //p2의 돈은 prevMoney2 + (prevMoney1-1000)

        //when
        GameResponse response = gameService.process(p1.getId(), currentB.getId(), roll);

        //then
        assertThat(response.isGameOver()).isFalse();
        assertThat(response.getGameOverId()).isEqualTo(null);
        assertThat(p1.getMoney()).isEqualTo(prevMoney - newB.getPrice());
        // 이전위치에서 플레이어가 제거 되고, 새로운 위치에 플레이어가 추가되어야함
        assertThat(currentB.getOnPlayers().contains(p1)).isFalse();
        assertThat(newB.getOnPlayers().contains(p1)).isTrue();
        // p1에서 차감된 만큼 p2에 지불되어야함
        assertThat(p2.getMoney()).isEqualTo(Math.abs(prevMoney2 + (prevMoney-1000)));
    }

    @Test
    @DisplayName("플레이어가 소유주가 아닐때 + 주인이 있는 땅일 경우 + gameover")
    void process2() {
        //given
        Player p1 = gameService.getPlayer(players.get(0).getId());
        int prevMoney = p1.getMoney();
        Player p2 = gameService.getPlayer(players.get(1).getId());
        int prevMoney2 = p2.getMoney();

        Board currentB = gameService.getBoard(2);
        currentB.getOnPlayers().add(p1); // p1 을 currentB에 위치시킴

        int roll = gameService.setRoll(p1.getId());
        Board newB = gameService.getBoard(currentB.getId() + roll);
        newB.setPrice(p1.getMoney() + 1000);
        newB.setOwner(p2);

        //when
        GameResponse response = gameService.process(p1.getId(), currentB.getId(), roll);

        //then
        assertThat(response.isGameOver()).isTrue();
        assertThat(response.getGameOverId()).isEqualTo(p1.getId());
        assertThat(p1.getMoney()).isEqualTo(prevMoney - newB.getPrice());
        // 이전위치에서 플레이어가 제거 되고, 새로운 위치에 플레이어가 추가되어야함
        assertThat(currentB.getOnPlayers().contains(p1)).isFalse();
        assertThat(newB.getOnPlayers().contains(p1)).isTrue();
        // p1에서 차감된 만큼 p2에 지불되어야함
        assertThat(p2.getMoney()).isEqualTo(prevMoney2 + (prevMoney+1000));
    }

    @Test
    @DisplayName("플레이어가 소유주가 아닐때 + 주인이 있는 땅일 경우 + 값 지불")
    void process3() {
        //given
        Player p1 = gameService.getPlayer(players.get(0).getId());
        Player p2 = gameService.getPlayer(players.get(1).getId());

        Board currentB = gameService.getBoard(2);
        currentB.getOnPlayers().add(p1);

        int roll = gameService.setRoll(p1.getId());
        Board newB = gameService.getBoard(currentB.getId() + roll);
        newB.setPrice(p1.getMoney() - 1000);
        newB.setOwner(p2);

        //when
        GameResponse response = gameService.process(p1.getId(), currentB.getId(), roll);

        //then
        assertThat(response.isGameOver()).isFalse();
        assertThat(response.getGameOverId()).isNull();
        assertThat(p1.getMoney()).isEqualTo(1000);
        // 이전위치에서 플레이어가 제거 되고, 새로운 위치에 플레이어가 추가되어야함
        assertThat(currentB.getOnPlayers().contains(p1)).isFalse();
        assertThat(newB.getOnPlayers().contains(p1)).isTrue();
    }

    @Test
    void isStartTrue() {
        // given
        // 2명의 플레이어가 존재함
        gameService.setReady("session1");
        gameService.setReady("session2");

        // when
        boolean res = gameService.isStart();

        // then
        assertThat(res).isTrue();
    }

    @Test
    void isStartFalse() {
        // given
        // 플레이어 1명 추가
        gameService.addPlayer("session3");
        players.add(gameService.getPlayer("session3"));

        gameService.setReady("session1");
        gameService.setReady("session2");
        gameService.setReady("session3");

        // when
        boolean res = gameService.isStart();

        // then
        assertThat(res).isFalse();
    }

    @Test
    @DisplayName("changeTurn 모두 false")
    void changeTurnAllFalse() {
        // given
        // 플레이어들 모두 turn false 상태

        // when
        Map<String, Player> res = gameService.changeTurn();

        // then
        boolean allFalse = true;
        for (Player p : res.values()) {
            if (p.isTurn()) allFalse = false;
        }
        assertThat(allFalse).isFalse();
    }

    @Test
    @DisplayName("changeTurn 모두 true")
    void changeTurnAllTrue() {
        // given
        // 플레이어들 모두 turn true 상태
        for (Player player : players) {
            Player savedPlayer = gameService.getPlayer(player.getId());
            savedPlayer.setTurn(true);
            gameService.setPlayer(savedPlayer);
        }

        // when then
        Assertions.assertThrows(RuntimeException.class, () -> {
            gameService.changeTurn();
        });
    }

    @Test
    @DisplayName("changeTurn 한명만 false")
    void changeTurnOneFalse() {
        // given
        String truePlayerId = "";
        for (Player player : players) {
            Player savedPlayer = gameService.getPlayer(player.getId());
            savedPlayer.setTurn(true);
            truePlayerId = savedPlayer.getId();
            gameService.setPlayer(savedPlayer);
            break;
        }

        // when
        Map<String, Player> res = gameService.changeTurn();

        // then
        Player truePlayer = new Player();
        Player falsePlayer = new Player();
        for (Player player : res.values()) {
            if (player.getId().equals(truePlayerId)) {
                truePlayer = gameService.getPlayer(player.getId());
            } else {
                falsePlayer = gameService.getPlayer(player.getId());
            }
        }

        // 턴이 뒤바뀌므로 true false 반대로 되어야함
        assertThat(truePlayer.isTurn()).isFalse();
        assertThat(falsePlayer.isTurn()).isTrue();
    }
}