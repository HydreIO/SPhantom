package sceat.gui.web;

import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import sceat.domain.network.Core;
import sceat.domain.trigger.PhantomTrigger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fr.aresrpg.api.PhantomApi;
import fr.aresrpg.commons.util.map.Map;
import fr.aresrpg.sdk.mc.ServerType;
import fr.aresrpg.sdk.util.Defqon;
import fr.aresrpg.sdk.util.OperatingMode;

public class VpsWebSocketServer extends WebSocketApplication implements PhantomTrigger.Trigger {

	private static class VpsCreateResponse {
		private ServerType type;
		private int amount;

		@SuppressWarnings("unused")
		public VpsCreateResponse(ServerType type, int amount) {
			this.type = type;
			this.amount = amount;
		}

		public ServerType getType() {
			return type;
		}

		public int getAmount() {
			return amount;
		}
	}

	private Broadcaster broadcaster = new OptimizedBroadcaster();
	private Gson gson = new GsonBuilder().create();

	VpsWebSocketServer() {
		PhantomTrigger.Trigger.n3w(this);
	}

	@Override
	public void onConnect(WebSocket socket) {
		super.onConnect(socket);
		for (Map.Entry<String, PhantomApi.VpsApi> e : PhantomApi.get().getAllVps().entrySet())
			socket.send(gson.toJson(toJsonObject(e.getKey(), e.getValue())));
	}

	@Override
	public void onMessage(WebSocket socket, String text) {
		super.onMessage(socket, text);
		VpsCreateResponse response = gson.fromJson(text, VpsCreateResponse.class);
		if (response.getAmount() > 5) return;
		Core.getInstance().forceDeployServer(response.getType(), response.getAmount());

	}

	private JsonObject toJsonObject(String label, PhantomApi.VpsApi vps) {
		JsonObject object = new JsonObject();
		object.add("label", new JsonPrimitive(label));
		object.add("state", new JsonPrimitive(vps.getState().name()));
		object.add("ram", new JsonPrimitive(vps.getAvailableRam()));
		object.add("ip", new JsonPrimitive(vps.getIp().getHostAddress()));
		object.add("user", new JsonPrimitive("unknown"));
		object.add("password", new JsonPrimitive("unknown"));
		object.add("created", new JsonPrimitive(vps.getCreatedInfos()));
		JsonArray array = new JsonArray();
		vps.getAllServers().forEach(s -> array.add(toJsonObject(s)));
		object.add("servers", array);
		return object;
	}

	private JsonObject toJsonObject(PhantomApi.ServerApi server) {
		JsonObject object = new JsonObject();
		object.add("label", new JsonPrimitive(server.getLabel()));
		object.add("maxPlayers", new JsonPrimitive(server.getMaxPlayers()));
		object.add("status", new JsonPrimitive(server.getStatus().name()));
		object.add("lastTimeout", new JsonPrimitive(server.getLastTimeout()));
		JsonArray array = new JsonArray();
		server.getPlayers().forEach(u -> array.add(new JsonPrimitive(u.toString())));
		object.add("playersNames", array);
		return object;
	}

	@Override
	public void handleDefcon(Defqon d) {
		// Ignore
	}

	@Override
	public void handleOpMode(OperatingMode o) {
		// Ignore
	}

	@Override
	public void handleVps(PhantomApi.VpsApi a) {
		broadcaster.broadcast(getWebSockets(), gson.toJson(toJsonObject(a.getLabel(), a)));
	}
}
