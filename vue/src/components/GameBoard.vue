<template>
    <div class="container">
        <!-- 게임판 -->
        <div class="board">
            <div v-for="(cell, index) in cells" :key="index" :id="'cell' + cell.id" :class="cell.id !== '' ? 'cell' : ''">
                <!-- 플레이어 1과 2 말 표시 -->
                <div v-if="player1.position === cell.id" class="player-token player1"></div>
                <div v-if="player2.position === cell.id" class="player-token player2"></div>
                {{ cell.id }}
            </div>
        </div>

        <!-- 플레이어 정보와 주사위 굴리기 -->
        <div class="game-info">
            <div class="player-info">
                <div class="player">
                    <h3>{{ player1.name }}</h3>
                    <p>money: <span>{{ player1.money }}</span></p>
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

export default {
    data() {
        return {
            cells: [
                { id: 5 }, { id: 6 }, { id: 7 }, { id: 8 }, { id: 9 },
                { id: 4 }, { id: '' }, { id: '' }, { id: '' }, { id: 10 },
                { id: 3 }, { id: '' }, { id: '' }, { id: '' }, { id: 11 },
                { id: 2 }, { id: '' }, { id: '' }, { id: '' }, { id: 12 },
                { id: 1 }, { id: 16 }, { id: 15 }, { id: 14 }, { id: 13 }
            ],
            player1: {
                name: "Player 1",
                money: 1000,
                position: 1 // 초기 위치를 id가 1인 셀로 설정
            },
            player2: {
                name: "Player 2",
                money: 1000,
                position: 1 // 초기 위치를 id가 1인 셀로 설정
            },
            diceResult: '-',
        };
    },
    methods: {
        readyGame() {
            alert("게임 준비 완료!");
        },
        rollDice() {
            const result = Math.floor(Math.random() * 6) + 1;
            this.diceResult = result;
        }
    }
};
</script>
