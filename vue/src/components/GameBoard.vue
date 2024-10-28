<template>
    <div class="container">
        <!-- 게임판 -->
        <div class="board">
            <div v-for="(cell, index) in cells" :key="index" :id="'cell' + cell.id" :class="cell.id !== '' ? 'cell' : ''">
                <!-- 플레이어 1과 2 말 표시 -->
                <div v-if="playerCellNum === cell.id" class="player-token player1"></div>
                <div v-if="enemyCellNum === cell.id" class="player-token player2"></div>
                {{ cell.id }}
            </div>
        </div>

        <!-- 플레이어 정보와 주사위 굴리기 -->
        <div class="game-info">
            <div class="player-info">
                <div class="player">
                    <h3>{{ player.id }}</h3>
                    <p>money: <span>{{ player.money }}</span></p>
                </div>
            </div>

            <div class="button-section">
                <button
                    @click="ready"
                    class="btn"
                    :disabled="player.ready">
                    {{ isStart ? '진행중' : player.ready ? '준비완료' : '준비' }}
                </button>
                <button @click="roll" class="btn" :disabled="!player.turn">주사위</button>
            </div>

            <!-- 주사위 결과 표시 -->
            <div class="dice-section">
                <p>roll: <span>{{ diceResult }}</span></p>
            </div>
        </div>
    </div>
</template>

<script>
import '../assets/css/game.css';

import SockJS from 'sockjs-client'
import { Client as StompClient } from '@stomp/stompjs'

export default {
    mounted() {
        this.connect();
        // 브라우저 종료 시 연결 해제
        window.addEventListener('beforeunload', this.disconnect);
    },

    beforeDestroy() {
        window.removeEventListener('beforeunload', this.disconnect);
    },

    data() {
        // TODO 1. v1 프론트 변수 설정 참고하여 api 에 맞도록 수정
        // TODO 2. ready 구현, roll 구현

        return {
            stompClient: null,
            // TODO 4의 배수 (16/4 + 1 = 5) 를 이용해 동적으로 cells 그리기
            cells: [
                { id: 5 }, { id: 6 }, { id: 7 }, { id: 8 }, { id: 9 },
                { id: 4 }, { id: '' }, { id: '' }, { id: '' }, { id: 10 },
                { id: 3 }, { id: '' }, { id: '' }, { id: '' }, { id: 11 },
                { id: 2 }, { id: '' }, { id: '' }, { id: '' }, { id: 12 },
                { id: 1 }, { id: 16 }, { id: 15 }, { id: 14 }, { id: 13 }
            ],
            isStart: false,
            playerCellNum: 1,
            enemyCellNum: 1,
            player: {
                id: '',
                gubun: '',
                money: 0,
                gameOver: false,
                ready: false,
                turn: false,
                roll: 0,
            },
            diceResult: '-',
        };
    },

    methods: {
        connect() {
            this.stompClient = new StompClient({
                webSocketFactory: () => new SockJS('http://localhost:8080/game/v2'),
                reconnectDelay: 0,
                debug: (str) => console.log(str)
            });

            // 연결 후처리
            this.stompClient.onConnect = (frame) => {
                console.log('Connected: ', frame);

                // 초기화 실행
                this.publish('/app/init');

                // 구독
                this.subscribeInit();
                this.subscribeReady();
                this.subscribeRoll();
                this.subscribeDisconnect();
            };
            this.stompClient.onStompError = (error) => {
                console.error(error);
            }
            if (!this.stompClient.active) {
                this.stompClient.activate();
            }
        },

        subscribeInit() {
            this.stompClient.subscribe('/user/queue/init', (message) => {
                const init = JSON.parse(message.body);
                console.log('init player - ' + init.player.id);
                this.player.id = init.player.id;
            });
        },

        subscribeReady() {
            // TODO 준비 성공하면 /app/isStart 호출하는 방법으로 수정하기
            this.stompClient.subscribe('/user/queue/ready', (message) => {
                const ready = JSON.parse(message.body);
                console.log('ready player - ' + ready.player.id);
                if (ready.menu === 'IS_START') {
                    this.isStart = true;
                }
                if (ready.status === 'FAIL') {
                    alert('ready fail');
                    throw new Error('ready fail');
                }
                this.player.ready = ready.player.ready;
            });
        },

        subscribeRoll() {

        },

        subscribeDisconnect() {
            this.stompClient.subscribe('/user/queue/disconnect', (message) => {
                const disconnect = JSON.parse(message.body);
                console.log('disconnect player - ' + disconnect.player.id);
            });
        },

        disconnect(event) {
            if (this.stompClient && this.stompClient.connected) {
                this.publish('/app/disconnect', this.player.id);
                this.stompClient.deactivate();
            }
            event.preventDefault();
            event.returnValue = '';
        },

        publish(destination, body) {
            if (body) {
                this.stompClient.publish({
                    destination: destination,
                    body: body
                });
            } else {
                this.stompClient.publish({
                    destination: destination
                });
            }
        },

        ready() {
            if (this.stompClient
                && this.stompClient.connected
                && this.player.id !== '') {

                this.publish('/app/ready', this.player.id);
            }
        },
        roll() {
            const result = Math.floor(Math.random() * 6) + 1;
            this.diceResult = result;
        }
    }
};
</script>
