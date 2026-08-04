
// HTTP サーバーを使います
import com.sun.net.httpserver.HttpServer;
// リクエストを受け取る型です
import com.sun.net.httpserver.HttpExchange;
// 入出力エラー用です
import java.io.IOException;
// 応答を書き出す型です
import java.io.OutputStream;
// 接続先を指定します
import java.net.InetSocketAddress;
// UTF-8 を使います
import java.nio.charset.StandardCharsets;

// アプリの入口です
public class App {
    // 起動処理です
    public static void main(String[] args) throws Exception {
        // 8080 番ポートで待ち受けます
【1】        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ルートパスの処理を登録します
【1】        server.createContext("/", exchange -> {
            // 返すメッセージです
【毎】            String message = "こんにちは、サーバー！";
            // UTF-8 のバイト列に変換します
【毎】            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // プレーンテキストとして返します
【毎】            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            // 成功応答と本文サイズを返します
【毎】           exchange.sendResponseHeaders(200, body.length);
            // 応答本文を書き込みます
【毎】            try (OutputStream os = exchange.getResponseBody()) {
【毎】                os.write(body);
【毎】            }
        });

        // サーバーを起動します
【1】        server.start();
        // 起動メッセージを表示します
【1】        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
        // メインスレッドを待機させます
【1】        Thread.currentThread().join();
    }
}
