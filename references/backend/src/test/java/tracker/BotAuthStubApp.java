package tracker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class BotAuthStubApp {
    public static void main(String[] args) throws Exception {
        var server = HttpServer.create(new InetSocketAddress(9967), 0);
        server.createContext("/sendAuthCode", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            for (String param : t.getRequestURI().getQuery().split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    System.out.println(entry[0] + " " + entry[1]);
                }
            }

            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.close();
        }
    }
}
