package sceat.gui.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class WebServer extends HttpHandler{
    public static final Pattern INCLUDE_PATERN = Pattern.compile("<INCLUDE>([^ ]*)</INCLUDE>");
    @Override
    public void service(Request request, Response response) throws Exception {
        if("/".equals(request.getPathInfo())){
            response.sendRedirect("/index.html");
            return;
        }
        InputStream stream = WebServer.class.getResourceAsStream("/web"+request.getPathInfo());
        if(stream == null){
            response.sendError(404);
            return;
        }
        response.getWriter().write(replaceIncludes(toString(stream)));
        response.getWriter().flush();
    }

    private String replaceIncludes(String page) throws IOException {
        Matcher matcher = INCLUDE_PATERN.matcher(page);
        String p = page;
        if(!matcher.find())
            return p;
        String m = matcher.group(1);
        InputStream in = WebServer.class.getResourceAsStream("/web/"+m);
        if(in == null)
            return p;
        p = p.replace("<INCLUDE>"+m+"</INCLUDE>", replaceIncludes(toString(in)));
        return replaceIncludes(p);
    }

    public static String toString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            sb.append(line).append("\n");
        return sb.toString();
    }
}
