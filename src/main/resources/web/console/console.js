var ws;
var consoleText = document.getElementById("console-text");
function blink() {
    $('.blink').fadeOut(500).fadeIn(500);
}

function fakeInput() {
    var ti = $("#inputText");
    var fti = $('#fakeTextInput');
    fti.keydown(function () {
        ti.text(">"+fti.val())
    });
    fti.keyup(function (e) {
        if(e.keyCode == 13){
            ws.send(fti.val().substr(0));
            fti.val("");
        }
        ti.text(">"+fti.val())
    });
}

function setupWebSockets() {
    ws = new WebSocket("ws://"+window.location.hostname+(window.location.port ? ':'+window.location.port: '')+"/console");
    ws.onmessage = function (evt) {
        consoleText.innerHTML = consoleText.innerHTML + evt.data.split("\n").join("</br>");
        scrollConsole();
    }
}

function scrollConsole(){
    consoleText.scrollTop = consoleText.scrollHeight;
}

$( document ).ready(function (){
    setInterval(blink,1000);
    fakeInput();
    setupWebSockets()
});