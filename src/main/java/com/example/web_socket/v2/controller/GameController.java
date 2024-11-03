package com.example.web_socket.v2.controller;

import com.example.web_socket.AbnormalTerminateException;
import com.example.web_socket.domain.Board;
import com.example.web_socket.domain.GameRequest;
import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.Player;
import com.example.web_socket.domain.enums.GameMenu;
import com.example.web_socket.domain.enums.GameStatus;
import com.example.web_socket.service.GameService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class GameController {

    private static final Logger logger = LogManager.getLogger(GameController.class);

    private final Set<String> sessions = Collections.synchronizedSet(new HashSet<>());
    // 보류중인 세션들 저장
    private final Set<String> pendingSessions = Collections.synchronizedSet(new HashSet<>());

    private final GameService gameService;

    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // 실제 live 인 세션들만 유지하도록 1분마다 확인
    @Scheduled(fixedRate = 60000)
    public void checkSessions() {
        logger.info("checkSessions()");
        logger.info("checkSessions() session.size() = {}", sessions.size());

        // 세션개수와 플레이어수가 다르면서(비정상종료 된 경우), 게임이 시작되어 있는 경우 강제 종료시켜야함
        if (sessions.size() != gameService.getPlayerCount()
                && gameService.isStart()) {
            abnormalTerminate();
        }

        // pendingSessions 초기화
        pendingSessions.clear();
        pendingSessions.addAll(sessions);

        // 각 세션에 ping 전송
        sessions.forEach(session -> {
            logger.info("session - {}", session);
            messagingTemplate.convertAndSend("/topic/ping/" + session, session);
        });

        // 3초 뒤 응답 보류중인 세션들 정리
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("pendingSession.size() = {}", pendingSessions.size());

                    // 응답 보류중인 세션들 제거
                    pendingSessions.forEach(pendingSession -> {
                        logger.info("pendingSession - {}", pendingSession);
                        sessions.remove(pendingSession);
                        logger.info("sessions.size() = {}", sessions.size());
                        gameService.removePlayer(pendingSession);
                    });
                } catch (RuntimeException e) {
                    logger.error("checkSessions.run", e);
                    throw new RuntimeException(e);
                } finally {
                    pendingSessions.clear();
                }
            }
        }, 3000);
    }

    /**
     * 비정상(진행중인 게임이 끝나기전, 플레이어의 세션 종료) 종료시
     * 모든 세션과 플레이어 제거 후 예외 발생시킴
     */
    private void abnormalTerminate() {
        try {
            gameService.clearPlayers();
            throw new AbnormalTerminateException("another player out");
        } catch (AbnormalTerminateException e) {
            logger.error("abnormalTerminate", e);
            throw new AbnormalTerminateException(e.getMessage());
        } finally {
            sessions.clear();
        }
    }

    @MessageMapping("/pong")
    public void receivePong(String session) {
        // 응답이 확인된 세션 제거
        pendingSessions.remove(session);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String error(Exception e) {
        logger.error(e.getMessage(), e);

        if (e.getClass() == AbnormalTerminateException.class) {
            messagingTemplate.convertAndSend("/topic/error", e.getMessage());
        }

        return e.getMessage();
    }

    @MessageMapping("/init")
    @SendToUser("/queue/init")
    public GameResponse initGame(SimpMessageHeaderAccessor headerAccessor) {
        logger.info("initGame() session.size() = {}", sessions.size());

        // 세션은 최대 2개까지, 그이상일 경우 예외 발생
        if (sessions.size() > 1) {
            throw new RuntimeException("max 2, init session");
        }

        // 플레이어 초기화
        String id = headerAccessor.getSessionId();
        messagingTemplate.convertAndSendToUser(id, "/queue/test", "zz");
        logger.info("init session id - {}", id);
        sessions.add(id);
        gameService.addPlayer(id);

        // 응답 객체 생성
        GameResponse gameResponse = new GameResponse();
        gameResponse.setMenu(GameMenu.INIT);
        gameResponse.setStatus(GameStatus.SUCCESS);
        gameResponse.setPlayer(gameService.getPlayer(id));
        gameResponse.setBoards(gameService.getBoards());

        return gameResponse;
    }

    @MessageMapping("/disconnect")
    @SendToUser("/queue/disconnect")
    public void disconnectGame(String id) {
        // 진행중인 게임인데 플레이어가 나갈 경우, 모든 세션 강제 종료 시켜야함
        if (gameService.isStart()) {
            abnormalTerminate();
        }

        // 세션과 플레이어 제거
        sessions.remove(id);
        gameService.removePlayer(id);

        logger.info("disconnectGame() session.size() = {}", sessions.size());
    }

    @MessageMapping("/ready")
    @SendToUser("/queue/ready")
    public GameResponse ready(String id) {
        GameResponse gameResponse = new GameResponse();
        boolean ready = gameService.setReady(id); // 준비
        gameResponse.setStatus(ready ? GameStatus.SUCCESS : GameStatus.FAIL);
        gameResponse.setMenu(GameMenu.READY);
        gameResponse.setPlayer(gameService.getPlayer(id));
        return gameResponse;
    }

    @MessageMapping("/isStart")
    public void isStart() {
        GameResponse gameResponse = new GameResponse();
        gameResponse.setMenu(GameMenu.IS_START);

        boolean isStart = gameService.isStart();
        if (isStart) {
            // 모든 세션이 준비가 된 경우 changeTurn 호출하여 턴 부여
            gameResponse.setStatus(GameStatus.SUCCESS);
            gameService.changeTurn();
        } else {
            gameResponse.setStatus(GameStatus.FAIL);
        }

        // 각 세션에 각자의 플레이어 객체를 담아 리턴
        for (String session : sessions) {
            gameResponse.setPlayer(gameService.getPlayer(session));
            messagingTemplate.convertAndSend("/topic/isStart/" + session, gameResponse);
        }
    }

    @MessageMapping("/process")
    public void process(GameRequest gameRequest) {
        Player player = gameService.getPlayer(gameRequest.getPlayer().getId());
        Board board = gameService.getBoard(gameRequest.getBoard().getId());

        // 주사위 숫자로 게임 프로세스 실행
        int roll = gameService.setRoll(player.getId());
        int curBoardNum = board.getId();
        GameResponse response = gameService.process(player.getId(), curBoardNum, roll);
        response.setStatus(GameStatus.SUCCESS);

        // 프로세스 후 게임오버일 경우
        if (response.isGameOver()) {
            response.setMenu(GameMenu.IS_GAME_OVER);
        } else {
            response.setMenu(GameMenu.PROCESS);
            gameService.changeTurn();
        }

        // 각 세션에 각자의 플레이어 객체를 담아 리턴
        for (String session : sessions) {
            response.setPlayer(gameService.getPlayer(session));
            messagingTemplate.convertAndSend("/topic/process/" + session, response);
        }
    }
}
