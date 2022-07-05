var stompClient = null ;
var websocket;
var name;
var roomId;
function setConnected (connected) {
    $("#connect").prop("disabled", connected) ;
    $("#disconnect").prop("disabled", !connected);
    if (connected){
        $("#conversation").show();
        $("#chat").show();
    }else{
        $("#conversation").hide();
        $("#chat").hide();
    }
    $("#greetings").html("");
}

function connect() {
    name = $("#name").val();
    if (!name){
        return;
    }
    roomId =$("#roomId").val();
    // var socket = new SockJS("/chat");
    //判断当前浏览器是否支持WebSocket
    if('WebSocket' in window){
        // websocket = new WebSocket("ws://localhost:8149/aq-power/threeWebsocket/" + name + "/"+roomId);
        // websocket = new WebSocket("wss://dev.flyh5.cn/aq-power/threeWebsocket/" + name + "/"+roomId);
        websocket = new WebSocket("wss://wxgame.jicf.net/tower_ningxia/threeWebsocket/" + name + "/"+roomId);
    }
    else{
        alert('Not support websocket')
    }
    setConnected(true);


    //连接发生错误的回调方法
    websocket.onerror = function(){
        setMessageInnerHTML("error");
    };

    //连接成功建立的回调方法
    websocket.onopen = function(event){
        heartCheck.start();
        setMessageInnerHTML("open");
    }

    //接收到消息的回调方法
    websocket.onmessage = function(event){
        heartCheck.reset();
        if (event.data.indexOf("HeartBeat") == -1){
            setMessageInnerHTML(event.data);
        }
    }

    //连接关闭的回调方法
    websocket.onclose = function(e){
        console.info(e);
        setMessageInnerHTML("close");
        setConnected(false);
    }
}


//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function(e){
    console.info(e);
    websocket.close();
    clearTimeout(heartCheck.timeoutObj);
}

//将消息显示在网页上
function setMessageInnerHTML(message){
    $("#greetings").append('<div>'+ message +'</div>')
    // document.getElementById('message').innerHTML += innerHTML + '<br/>';
}

//发送消息
function send(){
    var c = $("#content").val();
    c = c.split(",");
    var data = {
        // "type" : "answer",
        // "question_id" : c[0],
        // "answer_id" : c[1],
        // "time" : c[2]
        "type" : c[0],
        "topicId":c[1],
        "time" : c[2],
        "answerId":c[3],
        "answer1":c[4],
        "answer2":c[5],
    };
    websocket.send(JSON.stringify(data));
}

//发送下一题消息
function nextSend(){
    var c = $("#content").val();
    c = c.split(",");
    var data = {
        "type" : "isNext"
    };
    websocket.send(JSON.stringify(data));
}

var heartCheck = {
    timeout: 10000,//50s
    timeoutObj: null,
    isRuning: false,
    reset: function(){
        clearTimeout(this.timeoutObj);
        this.start();
    },
    start: function(){
        isRuning = true;
        this.timeoutObj = setTimeout(function(){
            websocket.send(JSON.stringify({'type': 'pong'}));
        }, this.timeout)
    }
}

$(function () {
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        clearTimeout(heartCheck.timeoutObj);
        isRuning = false;
        websocket.close();
        setConnected(false);
    });
    $("#send").click(function () {
        send();
    });

    $("#nextSend").click(function () {
        nextSend();
    });
});

// function disconnect(){
//     if (stompClient != null){
//         stompClient.disconnect();
//     }
//     setConnected(false);
// }

// function sendName() {
//     socket.send(JSON.stringify({'content':$('#content').val(),'to':$('#to').val()}));
//     // stompClient.send('/app/hello', {}, JSON.stringify({'name':$("#name").val(),'content':$("#content").val()}));
// }

// function showGreeting(message) {
//     $("#greetings").append('<div>'+ message.name+":"+message.content +'</div>')
// }

