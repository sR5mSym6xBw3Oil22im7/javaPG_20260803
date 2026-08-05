import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String message;
            int statusCode;
            String contentType = "text/plain; charset=UTF-8";

            if (path.equals("/hello")) {
                String query = exchange.getRequestURI().getRawQuery();
                System.out.println("query = " + query);
                String name = "ゲスト";
                if (query != null) {
                    name = URLDecoder.decode(query.substring("name=".length()), StandardCharsets.UTF_8);
                }
                message = "こんにちは、" + name + "さん！";
                statusCode = 200;
            } else if (path.equals("/menu")) {
                message = "今日の特別メニューはカレー";
                statusCode = 200;
            } else if (path.equals("/bye")) {
                message = "さようなら";
                statusCode = 200;
            } else if (path.equals("/todos")) {
                // 追加: 追加できる List にするため、ArrayList にします。
                List<String> todos = new ArrayList<>(Arrays.asList("牛乳を買う", "卵を買う", "パンを買う"));
                // 追加: 自分の Todo を 1 件足します。
                todos.add("自分の今日のTodo");
                // 追加: HTML を入れる変数 html に、<ul><li>...</li></ul> を組み立てます。
                String html = "<ul>";
                for (String todo : todos) {
                    html += "<li>" + todo + "</li>";
                }
                html += "</ul>";
                message = html;
                statusCode = 200;
                // 追加: /todos のときだけ HTML として返します。
                contentType = "text/plain; charset=UTF-8";
            } else {
                message = "ページが見つかりません";
                statusCode = 404;
            }

            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(statusCode, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.start();
        Thread.currentThread().join();
    }
}
