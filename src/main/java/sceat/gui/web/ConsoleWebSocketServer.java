package sceat.gui.web;

import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import sceat.Main;
import sceat.domain.shell.Input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleWebSocketServer extends WebSocketApplication implements Input.PhantomInput{
    private class LoggerHandler extends Handler{
        @Override
        public void publish(LogRecord record) {
            String l = '[' + FORMAT.format(new Date(record.getMillis())) + "]["
                    + Thread.currentThread().getName() + "][" + record.getLevel() +"]: "
                    + record.getMessage() + '\n' + throwableToString(record.getThrown());
            broadcaster.broadcast(getWebSockets() , l);
            logs.append(l);
        }

        @Override
        public void flush() {
            //No
        }

        @Override
        public void close() throws SecurityException {
            //no
        }
    }
    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private StringBuilder logs = new StringBuilder();
    private Broadcaster broadcaster = new OptimizedBroadcaster();

    public ConsoleWebSocketServer() {
        Main.getLogger().addHandler(new LoggerHandler());
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        super.onMessage(socket, text);
        Main.getLogger().log(Level.INFO , text);
        push(text);
    }

    @Override
    public void onConnect(WebSocket socket) {
        super.onConnect(socket);
        socket.send(logs.toString());
    }

    private String throwableToString(Throwable throwable){
        if(throwable == null)
            return "";
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()){
            throwable.printStackTrace(new PrintStream(stream));//NOSONAR
            return new String(stream.toByteArray());
        } catch (IOException e) {
            Main.getLogger().log(Level.WARNING , "Could'not close stream" , e);
            return null;
        }
    }
}
