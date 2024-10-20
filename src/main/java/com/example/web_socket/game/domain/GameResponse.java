package com.example.web_socket.game.domain;

import com.example.web_socket.game.domain.enums.GameMenu;
import com.example.web_socket.game.domain.enums.GameStatus;

import java.util.HashMap;
import java.util.Map;

public class GameResponse {

    private Player player;
    private Map<Integer, Board> boards = new HashMap<>();

    private String message;
    private GameStatus status;
    private GameMenu menu;
    private boolean gameOver;
    private String gameOverId;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Map<Integer, Board> getBoards() {
        return boards;
    }

    public void setBoards(Map<Integer, Board> boards) {
        this.boards = boards;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameMenu getMenu() {
        return menu;
    }

    public void setMenu(GameMenu menu) {
        this.menu = menu;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getGameOverId() {
        return gameOverId;
    }

    public void setGameOverId(String gameOverId) {
        this.gameOverId = gameOverId;
    }
}
