package com.example.web_socket.domain;

public class Player {

    private String id;
    private int gubun;
    private int money;
    private boolean ready;
    private boolean turn;
    private int roll;

    public int getGubun() {
        return gubun;
    }

    public void setGubun(int gubun) {
        this.gubun = gubun;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }
}