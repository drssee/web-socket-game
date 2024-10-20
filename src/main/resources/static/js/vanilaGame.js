let ws;
let curBoardNum = 1;
const tag = {
    player1Piece: document.createElement('div'),
    player2Piece: document.createElement('div'),
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

function connect() {
    ws = new WebSocket('ws://localhost:8080/game');
    let player = createPlayer();

    ws.onopen = function () {
        console.log('Connected to server');
    };

    ws.onmessage = function (e) {
        const res = JSON.parse(e.data);
        console.log('onMessageHandler called with menu:', res.menu);
        player = res.player;
        onMessageHandler(res, player);
    };

    ws.onclose = function (e) {
        console.log(e);
        console.log('Disconnected from server');
    }
}

function onMessageHandler(res, player) {
    const menu = res.menu;
    const status = res.status;

    switch (menu) {
        case 'INIT':
            if (status === 'SUCCESS') {
                player.id = res.player.id;
                console.log('init success');
                console.log('id: ' + player.id);

            } else if (status === 'FAIL') {
                console.log('init fail');

            } else if (status === 'ERROR') {
                console.error('init error');
            }
            break;
        case 'READY':
            if (status === 'SUCCESS') {
                console.log('ready success');
                tag.readyButton.innerText = '준비중';

            } else if (status === 'FAIL') {
                console.log('ready fail');

            } else if (status === 'ERROR') {
                console.error('ready error')
            }
            break;
        case 'IS_START':
            if (status === 'SUCCESS') {
                tag.readyButton.innerText = '진행중';

            } else if (status === 'FAIL') {
                console.log('is_start fail');

            } else if (status === 'ERROR') {
                console.error('is_start error');
            }
            break;
        case 'PROCESS':
            console.log('h')
            break;
        case 'IS_GAME_OVER':
            console.log('i2')
            break;
    }

    // 플레이어와 보드 값에 따라 화면을 그려야함
    render(res, player);
}

function render(res, player) {
    // 플레이어 이름 지정
    tag.playerId.innerText = player.id;
    // 플레이어 돈 지정
    tag.playerMoney.innerText = player.money
    // 주사위 값 지정
    tag.diceResult = player.roll;

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
    // 넘어온 boards 루프 돌려서 현재 사용자들의 위치에 말 옮기기
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
            id: curBoardNum
        }
    }
    ws.send(JSON.stringify(roll));
}

export default {
    connect,
    ready,
    roll
}