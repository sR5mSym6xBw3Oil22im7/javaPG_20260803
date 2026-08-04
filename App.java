// かんたんなHTTPサーバーを使います
import com.sun.net.httpserver.HttpServer;
// リクエストと返事を受け取る箱です
import com.sun.net.httpserver.HttpExchange;
// 入出力のエラー用です
import java.io.IOException;
// 文字を送るときの出口です
import java.io.OutputStream;
// どの住所・どの番号で待つかを指定します
import java.net.InetSocketAddress;
// UTF-8という文字コードを使います
import java.nio.charset.StandardCharsets;

// 実行するクラスの名前です
public class App {
    // ここから起動します
    public static void main(String[] args) throws Exception {
        // 8080番で待ち受けます
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // "/" に来たときの処理をここに直接書きます
        server.createContext("/", exchange -> {
            // 返す文字を message という変数に入れます
            String message = "こんにちは、サーバー！";
            // UTF-8でバイト列に変えます
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // 文字化けしにくいように指定します
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            // 200は成功、本文の長さも伝えます
            exchange.sendResponseHeaders(200, body.length);
            // 返事を入れる出口を開きます
            try (OutputStream os = exchange.getResponseBody()) {
                // 文字を送ります
                os.write(body);
            }
            // ここで出口を自動で閉じます
        });

        // サーバーを起動します
        server.start();
        // 起動メッセージをそのまま表示します
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
        // プログラムを待機状態にします
        Thread.currentThread().join();
    }
    // main はここまでです
}