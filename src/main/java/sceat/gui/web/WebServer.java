package sceat.gui.web;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.InputStream;

public class WebServer extends HttpHandler{
    @Override
    public void service(Request request, Response response) throws Exception {
        if(request.getPathInfo().equals("/")){
            response.sendRedirect("/index.html");
            return;
        }
        InputStream stream = WebServer.class.getResourceAsStream("/web"+request.getPathInfo());
        if(stream == null){
            response.sendError(404);
            return;
        }
        int read;
        byte[] buff = new byte[1024];

        while ((read = stream.read(buff, 0, buff.length)) != -1)
            response.getOutputStream().write(buff, 0, read);
        response.getOutputStream().flush();
    }
}
