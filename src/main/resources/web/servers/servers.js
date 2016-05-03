var servers = $("#servers");
var form = $(document.forms["newServer"]);
var serverButton = $("#new-server > a");

function addVps(vps) {
    $("#"+vps.label).remove();
    var li = $("<li></li>");
    li.attr("id" , vps.label);
    var list = $("<ul></ul>");
    list.addClass("vps-data");
    list.append("<li>Players: " + countPlayers(vps.servers)+ "</li>");
    list.append("<li>State: " + vps.state + "</li>");
    list.append("<li>Ram: " + vps.ram + "</li>");
    list.append("<li>Ip: " + vps.ip + "</li>");
    list.append("<li>User: " + vps.user + "</li>");
    list.append("<li>Passwd: " + vps.password + "</li>");
    list.append("<li>Created: " + vps.created + "</li>");
    var serverList = $("<ul class='mcservers'></ul>");
    vps.servers.forEach(function (e) {
        addServer(e , serverList);
    });
    li.append("<h2>"+vps.label+"</h2>");
    li.append(list);
    li.append(serverList);
    servers.append(li);

}

function countPlayers(serverArray) {
    var i = 0;
    serverArray.forEach(function (e) {
        i+= e.playersNames.length;
    });
    return i;
}

function addServer(server , parentList) {
    if(server == null)
        return;
    var element = $("<li></li>");
    element.attr("id" , server.label);
    var list = $("<ul></ul>");
    list.append("<li tooltip='"+server.playersNames.join("&#xa;")+"'>Players: " + server.playersNames.length + "</li>");
    list.append("<li>MaxPlayers: " + server.maxPlayers + "</li>");
    list.append("<li>Status: " + server.status + "</li>");
    list.append("<li>LastTimeout: " + server.lastTimeout + "</li>");
    element.append("<h3>"+server.label+"</h3>");
    element.append(list);
    parentList.append(element);
}

function setupWebSockets() {
    ws = new WebSocket("ws://"+window.location.hostname+(window.location.port ? ':'+window.location.port: '')+"/servers");
    ws.onmessage = function (evt) {
        addVps(JSON.parse(evt.data));
    }
}

$( document ).ready(function (){
    setupWebSockets()
});

function createServer(){
    var domForm = form.get(0);
    console.log(JSON.stringify({
        type: domForm["serverType"].value,
        amount: parseInt(domForm["amount"].value)
    }));
    return false;
}

function test() {
    var vps = {
        label: "vps01",
        state: "opening",
        ram: "15",
        ip:"0.0.0.0",
        user:"fuck",
        password:'you',
        created: "15-9",
        servers: [createTestServer(),createTestServer(),createTestServer(),createTestServer(),createTestServer(),createTestServer()]
    };
    addVps(vps);
}

function createTestServer() {
    return {
        label: "vps01",
        playersNames: ["Hello1","Hello2","Hello3"],
        status: "opening",
        maxPlayers: 120,
        lastTimeout: 10
    };
}


function changeNewServerState() {

    if(form.hasClass("hidden")){
        form.removeClass("hidden");
        serverButton.children().removeClass("fa-angle-double-down");
        serverButton.children().addClass("fa-angle-double-up");
    }else{
        form.addClass("hidden");
        serverButton.children().removeClass("fa-angle-double-up");
        serverButton.children().addClass("fa-angle-double-down");
    }
}