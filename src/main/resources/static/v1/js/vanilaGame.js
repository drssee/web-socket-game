let ws;
let playerBoardNum = 1;
let enemyBoardNum = 1;
const playerBoards = [];
const enemyBoards = [];
const closeStatus = {
    NO_CLOSE_FRAME: 1006,
    SERVER_ERROR: 1011
}
const defaultErrorMsg = '처리중 예외가 발생하였습니다.';
const tag = {
    player1Piece: document.createElement('div'), // 플레이어1 말
    player2Piece: document.createElement('div'), // 플레이어2 말
    playerId: document.getElementById('player-id'),
    playerMoney: document.getElementById('player-money'),
    readyButton: document.getElementById('ready-button'),
    diceButton: document.getElementById('roll-dice-button'),
    diceResult: document.getElementById('dice-result')
}

function createPlayer() {
    return {
        id: '',
        gubun: '',
        money: 0,
        gameOver: false,
        ready: false,
        turn: false,
        roll: 0
    }

}

function init() {
    tag.player1Piece.classList.add('player1-piece');
    tag.player2Piece.classList.add('player2-piece');
    document.getElementById('cell1').appendChild(tag.player1Piece);
    document.getElementById('cell1').appendChild(tag.player2Piece);
    tag.readyButton.addEventListener('click', ready);
    tag.diceButton.addEventListener('click', roll);
}

function connect() {
    ws = new WebSocket('ws://localhost:8080/game/v1');
    let player = createPlayer();

    ws.onopen = function () {
        console.log('Connected to server');
    };

    ws.onmessage = function (e) {
        const res = JSON.parse(e.data);
        player = res.player;
        onMessageHandler(res, player);
    };

    ws.onclose = function (e) {
        console.log(e);
        if (e.code === closeStatus.NO_CLOSE_FRAME || e.code === closeStatus.SERVER_ERROR) {
            alert(e.reason || defaultErrorMsg);
        }
        console.log('Disconnected from server');
    }
}

function onMessageHandler(res, player) {
    const menu = res.menu;
    const status = res.status;

    console.log(menu + ' ' + status);

    switch (menu) {
        case 'PROCESS':
            // boards 돌면서 자신과 상대 위치 업데이트
            console.log(res);
            Object.values(res.boards).forEach(b => {
                Object.values(b.onPlayers).forEach(op => {
                    if (op.id === player.id) {
                        playerBoardNum = Number(b.id);
                    } else {
                        enemyBoardNum = Number(b.id);
                    }
                });

                if (b.owner) {
                    if (b.owner.id == res.player.id && !playerBoards.includes(b.id)) {
                        playerBoards.push(b.id);
                    } else if (b.owner.id != res.player.id && !enemyBoards.includes(b.id)) {
                        enemyBoards.push(b.id);
                    }
                }
            });

            break;
        case 'IS_GAME_OVER':
            alert(res.gameOverId !== player.id ? '승리' : '패배');
            break;
    }

    render(res, player);
}

function render(res, player) {
    // 플레이어 이름 지정
    tag.playerId.innerText = player.id;
    // 플레이어 돈 지정
    tag.playerMoney.innerText = player.money
    // 주사위 값 지정
    tag.diceResult.innerText = player.roll;

    // 게임오버일 경우 모든 버튼 비활성화
    if (res.gameOver) {
        tag.readyButton.innerText = '종료';
        tag.readyButton.disabled = true;
        tag.diceButton.disabled = true;
        return;
    }

    // 준비, 시작에 따라 버튼 수정
    if (res.menu === 'READY' && res.status === 'SUCCESS') {
        tag.readyButton.innerText = '준비중';
    }
    if (res.menu === 'IS_START' && res.status === 'SUCCESS') {
        tag.readyButton.innerText = '진행중';
    }

    // 준비, 턴에 따라 버튼 disable 처리
    if (player.ready) {
        tag.readyButton.disabled = true;
    } else if (!player.ready) {
        tag.readyButton.disabled = false;
    }
    if (player.turn) {
        tag.diceButton.disabled = false;
    } else if (!player.turn) {
        tag.diceButton.disabled = true;
    }

    // 플레이어 말 이동 + 보드 최신화
    document.getElementById('cell' + playerBoardNum).appendChild(tag.player1Piece);
    document.getElementById('cell' + enemyBoardNum).appendChild(tag.player2Piece);
    playerBoards.forEach(e => {
       document.getElementById('cell' + e).classList.add('player1-board');
    });
    enemyBoards.forEach(e => {
        document.getElementById('cell' + e).classList.add('player2-board');
    });
}

function validateWs() {
    return ws && ws.readyState === WebSocket.OPEN;
}

function ready() {
    if (!validateWs()) return;
    const ready = {
        command: 'ready'
    }
    ws.send(JSON.stringify(ready));
}

function roll() {
    if (!validateWs()) return;
    const roll = {
        command: 'roll',
        board: {
            id: playerBoardNum
        }
    }
    ws.send(JSON.stringify(roll));
}

export default {
    init,
    connect
}