import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            String message;
            int statusCode;

            if (path.equals("/hello")) {
                // URL のクエリ（?name=Taro の部分）をそのまま取ります。
                String query = exchange.getRequestURI().getRawQuery();
                // いったん画面に出して、今の値を確認します。
                System.out.println("query = " + query);
                // name= の後ろだけを切り出します。
                String name = query.substring("name=".length());
                // 取り出した名前を返事の文に入れます。
                message = "こんにちは、" + name + "さん！";
                statusCode = 200;
            } else if (path.equals("/menu")) {
                // 今日の定食を返します。
                message = "今日の定食はカレー";
                statusCode = 200;
            } else if (path.equals("/bye")) {
                // さようならを返します。
                message = "さようなら！";
                statusCode = 200;
            } else {
                // 見つからないときの文を返します。
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
