let ws;
let dice = 0;
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
        case 'INIT': // 초기화된 세션에게 별도의 응답 메시지를 전달함
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
            if (status === 'SUCCESS' && player.ready) {
                console.log('ready success');
                isStart();

            } else if (status === 'ERROR') {
                console.error('ready error')
            }
            break;
        case 'IS_START':
            if (status === 'SUCCESS') {
                tag.readyButton.innerText = '진행중';
                changeTurn();
            }
            // 실패인데 레디한 사람한텐 alert 줘야함
            else if (status === 'FAIL' && player.ready) {
                tag.readyButton.innerText = '준비중';
                if (player.ready) {
                    alert('ready 1/2');
                }
            }

            else if (status === 'ERROR') {
                console.error('is_start error');
            }
            break;
        case 'CHANGE_TURN':
            // 1. 체인지턴 호출
            // 2. 임의 플레이어 1명에게 턴 배분
            // 3. 턴 배분받음 플레이어와, 받지 못한 플레이어의 턴 상태값이 반대여야함
            if (status === 'SUCCESS' && player.turn) {
                console.log(player.id + "`s turn - true");

            } else if (status === 'SUCCESS' && !player.turn) {
                console.log(player.id + "`s turn - false");

            } else if (status === 'FAIL') {
                console.log('change_turn fail');

            } else if (status === 'ERROR') {
                console.error('change_turn error');
            }
            break;
        case 'ROLL':
            // dice에 주사위 값 주입하고 render 구현
            if (status === 'SUCCESS') {
                dice = player.roll;
                console.log('roll success - ' + dice);
                //handle();
                changeTurn();
            } else {
                console.log('roll fail');
            }
            break;
        case 'HANDLE':
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
    // 플레이어 말 이동
    // 응답에서 플레이어가 소속된 boardList와, Player객체에 현재 위치 저장하여 전달?

    // 턴에 따라 버튼 disable 처리
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

function isStart() {
    if (!validateWs()) return;
    const isStart = {
        command: 'isStart'
    }
    ws.send(JSON.stringify(isStart));
}

function roll() {
    if (!validateWs()) return;
    const roll = {
        command: 'roll'
    }
    ws.send(JSON.stringify(roll));
}

function changeTurn() {
    if (!validateWs()) return;
    const changeTurn = {
        command: 'changeTurn'
    }
    ws.send(JSON.stringify(changeTurn));
}

export default {
    connect,
    ready,
    isStart,
    roll,
    changeTurn
}