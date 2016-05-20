package sceat.gui.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fr.aresrpg.commons.concurrent.Threads;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import sceat.domain.shell.Input;

public class GrizzlyWebServer {

	private static GrizzlyWebServer instance;
	private HttpServer server;
	private Input.PhantomInput input;

	public GrizzlyWebServer(int port) throws IOException {
		instance = this;
		this.server = HttpServer.createSimpleServer(null, port);
		this.server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
		server.getServerConfiguration().addHttpHandler(new WebServer(), "/*");
		WebSocketEngine.getEngine().register("", "/servers", new VpsWebSocketServer());
		ConsoleWebSocketServer s = new ConsoleWebSocketServer();
		input = s;
		WebSocketEngine.getEngine().register("", "/console", s);
		this.server.start();
	}

	public Input.PhantomInput getInput() {
		return input;
	}

	public static void stop() {
		instance.server.shutdown();
	}

	public static void main(String[] args) throws IOException {
		new GrizzlyWebServer(8080);
		Threads.uSleep(Integer.MAX_VALUE , TimeUnit.DAYS);
	}

}
