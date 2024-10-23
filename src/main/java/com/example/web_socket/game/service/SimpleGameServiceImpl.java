package com.example.web_socket.game.service;

import com.example.web_socket.game.domain.Board;
import com.example.web_socket.game.domain.GameResponse;
import com.example.web_socket.game.domain.Player;

import java.util.*;

public class SimpleGameServiceImpl implements GameService {

    private final int START = 1;
    private final int BASIC_BOARD_SIZE = 16;
    private final int BASIC_PRICE = 10000;
    private final int BASIC_MONEY = 100000;
    private int gubun = 1;

    private Map<String, Player> players = new HashMap<>();
    private Map<Integer, Board> boards = new HashMap<>();

    public SimpleGameServiceImpl() {
        this.initBoard(BASIC_BOARD_SIZE);
    }

    // TODO 예외 모아서 따로 응답 하도록 처리해야함
    private void validate(Player player) {
        if (player == null) throw new RuntimeException("player is null");
    }

    private void validate(Board board) {
        if (board == null) throw new RuntimeException("board is null");
    }

    private void validate(Player player, Board board) {
        if (player == null || board == null) throw new RuntimeException("player or board is null");
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public Board getBoard(int boardNum) {
        return boards.get(boardNum);
    }

    @Override
    public void initBoard(int boardSize) {
        for (int i=0; i<boardSize; i++) {
            Board board = new Board();
            board.setId(i+1);
            board.setName("board"+(i+1));
            board.setPrice((int) (Math.random() * BASIC_PRICE) + 1);
            boards.put(i+1, board);
        }
    }

    @Override
    public void addPlayer(String playerId) {
        Player player = new Player();
        player.setId(playerId);
        player.setMoney(BASIC_MONEY);
        player.setGubun(gubun++);
        this.players.put(playerId, player);

        // 보드에 플레이어 배치
        boards.get(1).getOnPlayers().add(player);
    }

    @Override
    public void setPlayer(Player player) {
        this.players.put(player.getId(), player);
    }

    @Override
    public void removePlayer(String playerId) {
        this.players.remove(playerId);
    }

    @Override
    public boolean setReady(String playerId) {
        Player player = players.get(playerId);
        if (player == null) return false;
        player.setReady(true);
        return true;
    }

    @Override
    public boolean isStart() {
        if (players.size() % 2 != 0) return false;
        for (Player player : players.values()) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    @Override
    public GameResponse process(String playerId, int curBoardNum, int roll) {
        Player player = players.get(playerId);
        Board curBoard = boards.get(curBoardNum);
        validate(player, curBoard);

        // 주사위 값을 이용해 다음 보드 검증
        int rolledNum = curBoardNum + roll;
        Board nextBoard = boards.get(
                rolledNum > BASIC_BOARD_SIZE ? rolledNum - BASIC_BOARD_SIZE : rolledNum
        );
        validate(nextBoard);

        // 이전 board 에서 플레이어 제거 + 새로운 board 에 플레이어 추가
        curBoard.getOnPlayers().remove(player);
        nextBoard.getOnPlayers().add(player);

        GameResponse gameResponse = new GameResponse();

        // 플레이어가 소유주일때
        if (nextBoard.getOwner() != null
                && nextBoard.getOwner().getId().equals(player.getId())) {

            // TODO 추가 기능 필요할 경우 추가
        }

        // 플레이어가 소유주가 아닐때
        else {
            // 주인이 없는 땅이면
            if (nextBoard.getOwner() == null) {
                // TODO 웹소켓과 연계해서 구매 선택여부 알아야함 + 테스트 구현
                // 전처리 -> 구매여부확인 -> 후처리 하면 가능할듯?
                // 일단은 도착해서 돈이 있으면 무조건 구매하도록?
                
            }
            // 주인이 있는 땅이면
            else {
                if (isGameOver(playerId, nextBoard.getId())) {
                    gameResponse.setGameOver(true);
                    gameResponse.setGameOverId(playerId);
                }
                player.setMoney(player.getMoney() - nextBoard.getPrice());
                gameResponse.setPlayer(player);
            }
        }

        gameResponse.setBoards(boards);
        return gameResponse;
    }

    @Override
    public int setRoll(String playerId) {
        int roll = (int) (Math.random() * 6) + 1;
        Player player = players.get(playerId);
        player.setRoll(roll);
        return roll;
    }

    @Override
    public boolean isGameOver(String playerId, int boardNum) {
        if (boardNum == START) return false;

        Player player = players.get(playerId);
        Board board = boards.get(boardNum);
        validate(player, board);

        int needMoney = board.getPrice();
        if (board.getOwner() != null
                && board.getOwner() != player
                && player.getMoney() < needMoney) {

            return true;
        }
        return false;
    }

    @Override
    public boolean buy(String playerId, int boardNum) {
        Player player = players.get(playerId);
        Board board = boards.get(boardNum);
        validate(player, board);

        //소유주 없는 땅이고 돈이 있으면 구매
        if (board.getOwner() == null && player.getMoney() >= board.getPrice()) {
            board.setOwner(player);
            player.setMoney(player.getMoney() - board.getPrice());
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Player> changeTurn() {
        // 초기상태 즉 플레이어들의 턴상태가 모두 false 이면 랜덤 플레이어 true 로 수정
        boolean hasTrue = false;
        for (Player p : players.values()) {
            if (hasTrue && p.isTurn()) throw new RuntimeException("cant be all true");
            if (p.isTurn()) hasTrue = true;
        }

        if (hasTrue) {
            for (Player p : players.values()) {
                p.setTurn(!p.isTurn());
            }
        } else {
            // 랜덤 플레이어 턴 true 로 변경
            List<String> keys = new ArrayList<>(players.keySet());
            int random = new Random().nextInt(keys.size());
            players.get(keys.get(random)).setTurn(true);
        }
        return players;
    }
}
