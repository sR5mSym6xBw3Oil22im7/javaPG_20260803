import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            // パスを取り出して path という変数に入れます。
            String path = exchange.getRequestURI().getPath();

            String message;
            int statusCode;

            // path と文字列を比べます。
            if (path.equals("/hello")) {
                message = "こんにちは！";
                statusCode = 200;
            } else if (path.equals("/bye")) {
                message = "さようなら！";
                statusCode = 200;
            } else {
                message = "ページが見つかりません";
                statusCode = 404;
            }

            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.start();
        Thread.currentThread().join();
    }
}
