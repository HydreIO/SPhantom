package sceat.gui.web;

import com.google.gson.*;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import sceat.domain.adapter.api.PhantomApi;

import java.util.Map;

public class VpsWebSocketServer extends WebSocketApplication{

    private Broadcaster broadcaster = new OptimizedBroadcaster();
    private Gson gson = new GsonBuilder().create();

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        //Ignore messages
    }

    @Override
    public void onConnect(WebSocket socket) {
        super.onConnect(socket);
        for(Map.Entry<String , PhantomApi.VpsApi> e : PhantomApi.getAllVps().entrySet())
            socket.send(gson.toJson(toJsonObject(e.getKey() , e.getValue())));

    }

    private JsonObject toJsonObject(String label , PhantomApi.VpsApi vps){
        JsonObject object = new JsonObject();
        object.add("label" , new JsonPrimitive(label));
        object.add("state" , new JsonPrimitive(vps.getState().name()));
        object.add("ram" , new JsonPrimitive(vps.getAvailableRam()));
        object.add("ip" , new JsonPrimitive(vps.getIp().getHostAddress()));
        object.add("user" , new JsonPrimitive("unknown"));
        object.add("password" , new JsonPrimitive("unknown"));
        object.add("created" , new JsonPrimitive(vps.getCreatedInfos()));
        JsonArray array = new JsonArray();
        vps.getAllServers().forEach(s -> array.add(toJsonObject(s)));
        object.add("servers" , array);
        return object;
    }

    private JsonObject toJsonObject(PhantomApi.ServerApi server){
        JsonObject object = new JsonObject();
        object.add("label" , new JsonPrimitive(server.getLabel()));
        object.add("maxPlayers" , new JsonPrimitive(server.getMaxPlayers()));
        object.add("status" , new JsonPrimitive(server.getStatus().name()));
        object.add("lastTimeout" , new JsonPrimitive(server.getLastTimeout()));
        JsonArray array = new JsonArray();
        server.getPlayers().forEach(u -> array.add(new JsonPrimitive(u.toString())));
        object.add("playersNames" , array);
        return object;
    }
}
