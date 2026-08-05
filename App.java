// HTTP サーバーを使うための部品です。
import com.sun.net.httpserver.HttpServer;
// 画面に返す文字を送るための部品です。
import java.io.OutputStream;
// 通信するための住所を作る部品です。
import java.net.InetSocketAddress;
// 文字化けしないように UTF-8 を使います。
import java.nio.charset.StandardCharsets;

// アプリの本体です。
public class App {
    // 最初に動く場所です。
    public static void main(String[] args) throws Exception {
        // 8080 番ポートでサーバーを作ります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // / へのアクセスを受け取る処理を作ります。
        server.createContext("/", exchange -> {
            // アクセスされたパスを取り出して path に入れます。
            String path = exchange.getRequestURI().getPath();

            // 返す文章を入れる箱です。
            String message;
            // 返す HTTP ステータスを入れる箱です。
            int statusCode;

            // path の値ごとに分けます。
            if (path.equals("/hello")) {
                // こんにちは を返します。
                message = "こんにちは！";
                // 成功を返します。
                statusCode = 200;
            } else if (path.equals("/menu")) {
                // 今日の定食はカレー を返します。
                message = "今日の定食はカレー";
                // 成功を返します。
                statusCode = 200;
            } else if (path.equals("/bye")) {
                // さようなら を返します。
                message = "さようなら！";
                // 成功を返します。
                statusCode = 200;
            } else {
                // 見つからないメッセージを返します。
                message = "ページが見つかりません";
                // 見つからないエラーを返します。
                statusCode = 404;
            }

            // 文字列をバイト列に変えます。
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // UTF-8 で返すことを伝えます。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            // ステータスと長さを送ります。
            exchange.sendResponseHeaders(statusCode, body.length);
            // 返す本文を書き込みます。
            try (OutputStream os = exchange.getResponseBody()) {
                // 本文を送ります。
                os.write(body);
            }
        });

        // サーバーを起動します。
        server.start();
        // プログラムがすぐ終わらないように待ちます。
        Thread.currentThread().join();
    }
}
