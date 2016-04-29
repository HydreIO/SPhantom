package sceat.gui.web;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import sceat.domain.shell.Input;

import java.io.IOException;

public class GrizzlyWebServer {

    private HttpServer server;
    private Input.PhantomInput input;

    public GrizzlyWebServer(int port) throws IOException {
        this.server = HttpServer.createSimpleServer(null , port);
        this.server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
        server.getServerConfiguration().addHttpHandler(new WebServer() , "/*");
        WebSocketEngine.getEngine().register("" , "/servers" , new VpsWebSocketServer());
        ConsoleWebSocketServer s = new ConsoleWebSocketServer();
        input = s;
        WebSocketEngine.getEngine().register("" , "/console" , s);
        this.server.start();
    }

    public Input.PhantomInput getInput() {
        return input;
    }

    public void stop(){
        this.server.shutdown();
    }

}
