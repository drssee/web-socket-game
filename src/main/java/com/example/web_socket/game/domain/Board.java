package com.example.web_socket.game.domain;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private int id;
    private String name;
    private String useYn;
    private Player owner;
    private int price;
    private List<Player> onPlayers = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Player> getOnPlayers() {
        return onPlayers;
    }

    public void setOnPlayers(List<Player> onPlayers) {
        this.onPlayers = onPlayers;
    }
}
