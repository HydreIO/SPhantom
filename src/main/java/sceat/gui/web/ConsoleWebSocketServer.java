package sceat.gui.web;

import java.io.IOException;

import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import sceat.domain.shell.Input;
import fr.aresrpg.commons.domain.log.handler.BaseHandler;
import fr.aresrpg.commons.domain.log.handler.formatters.BasicFormatter;
import fr.aresrpg.sdk.system.Log;

public class ConsoleWebSocketServer extends WebSocketApplication implements Input.PhantomInput {
	private class LoggerHandler extends BaseHandler {
		@Override
		public void handle(fr.aresrpg.commons.domain.log.Log log) throws IOException {
			String l = format(log);
			broadcaster.broadcast(getWebSockets(), l);
			logs.append(l);
		}
	}

	private StringBuilder logs = new StringBuilder();
	private Broadcaster broadcaster = new OptimizedBroadcaster();

	public ConsoleWebSocketServer() {
		LoggerHandler handler = new LoggerHandler();
		handler.setFormatter(new BasicFormatter());
		Log.getInstance().getLogger().addHandler(handler);
	}

	@Override
	public void onMessage(WebSocket socket, String text) {
		super.onMessage(socket, text);
		Log.out(text);
		push(text);
	}

	@Override
	public void onConnect(WebSocket socket) {
		super.onConnect(socket);
		socket.send(logs.toString());
	}

}
