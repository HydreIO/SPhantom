var ws;
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
    var console = document.getElementById("console-text");
    ws.onmessage = function (evt) {
        console.innerHTML = console.innerHTML + evt.data.replace("\n" ,"</br>" );
    }
}

$( document ).ready(function (){
    setInterval(blink,1000);
    fakeInput();
    setupWebSockets()
});