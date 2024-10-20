package com.example.web_socket.game.domain;

import com.example.web_socket.game.domain.enums.GameMenu;
import com.example.web_socket.game.domain.enums.GameStatus;

import java.util.ArrayList;
import java.util.List;

public class GameResponse {

    private Board board;
    private Player player;
    private List<Board> boards = new ArrayList<>();

    private int data;
    private String message;
    private GameStatus status;
    private GameMenu menu;

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
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
}
