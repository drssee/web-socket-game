<template>
    <div class="container">
        <!-- 게임판 -->
        <div class="board">
            <div v-for="(cell, index) in cells" :key="index" :id="'cell' + cell.id" :class="cell.id !== '' ? 'cell' : ''">
                <!-- 플레이어 1과 2 말 표시 -->
                <div v-if="player.position === cell.id" class="player-token player1"></div>
<!--                <div v-if="player2.position === cell.id" class="player-token player2"></div>-->
                {{ cell.id }}
            </div>
        </div>

        <!-- 플레이어 정보와 주사위 굴리기 -->
        <div class="game-info">
            <div class="player-info">
                <div class="player">
                    <h3>{{ player.name }}</h3>
                    <p>money: <span>{{ player.money }}</span></p>
                </div>
            </div>

            <div class="button-section">
                <button @click="readyGame" class="btn">준비</button>
                <button @click="rollDice" class="btn">주사위</button>
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
            cells: [
                { id: 5 }, { id: 6 }, { id: 7 }, { id: 8 }, { id: 9 },
                { id: 4 }, { id: '' }, { id: '' }, { id: '' }, { id: 10 },
                { id: 3 }, { id: '' }, { id: '' }, { id: '' }, { id: 11 },
                { id: 2 }, { id: '' }, { id: '' }, { id: '' }, { id: 12 },
                { id: 1 }, { id: 16 }, { id: 15 }, { id: 14 }, { id: 13 }
            ],
            player: {
                id: '',
                position: 1 // 초기 위치를 id가 1인 셀로 설정
            },
            diceResult: '-',
        };
    },

    methods: {
        connect() {
            this.stompClient = new StompClient({
                webSocketFactory: () => new SockJS('http://localhost:8080/game/v2'),
                reconnectDelay: 5000,
                debug: (str) => console.log(str)
            });
            this.stompClient.onConnect = (frame) => {
                console.log('Connected: ', frame);
                this.publish('/app/init');
                this.subscribe();
            };
            this.stompClient.onStompError = (error) => {
                console.error(error);
            }
            this.stompClient.activate();
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

        subscribe() {
            this.stompClient.subscribe('/user/queue/init', (message) => {
                const gameResponse = JSON.parse(message.body);
                console.log('Received message:', gameResponse);
                this.player.id = gameResponse.player.id;
                console.log('init player - ' + this.player.id);
            });
            this.stompClient.subscribe('/user/queue/disconnect', (message) => {
                const gameResponse = JSON.parse(message.body);
                console.log('Received message:', gameResponse);
                console.log('disconnect player - ' + gameResponse.player.id);
            });
        },

        readyGame() {
            if (this.stompClient && this.stompClient.connected) {

            }
        },
        rollDice() {
            const result = Math.floor(Math.random() * 6) + 1;
            this.diceResult = result;
        }
    }
};
</script>
