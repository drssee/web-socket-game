package com.example.web_socket.service;

import com.example.web_socket.domain.Board;
import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.Player;

import java.util.Map;

public interface GameService {
    void addPlayer(String playerId);
    void setPlayer(Player player);
    void removePlayer(String playerId);
    void initBoard(int boardSize);
    Player getPlayer(String playerId);
    Map<Integer, Board> getBoards();
    Board getBoard(int boardNum);
    boolean setReady(String playerId);

    int setRoll(String playerId);
    GameResponse process(String playerId, int curBoardNum, int roll);
    boolean isGameOver(String playerId, int boardNum);
    boolean buy(String playerId, int boardNum);
    boolean isStart();
    Map<String, Player> changeTurn();
    int getPlayerCount();
    void clearPlayers();
}
