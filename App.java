// HTTP サーバーを使うための部品です。
import com.sun.net.httpserver.HttpServer;
// 返事の本文を書き出すための部品です。
import java.io.OutputStream;
// 8080 番ポートを指定するための部品です。
import java.net.InetSocketAddress;
// URL の文字を元に戻すための部品です。
import java.net.URLDecoder;
// 文字を UTF-8 に変えるための部品です。
import java.nio.charset.StandardCharsets;
// App という名前のクラスです。
public class App {
    // 最初に動く場所です。
    public static void main(String[] args) throws Exception {
        // 8080 番ポートで待ち受けるサーバーを作ります。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // / に来たアクセスの処理を登録します。
        server.createContext("/", exchange -> {
            // 今きたリクエストのパスを取り出します。
            String path = exchange.getRequestURI().getPath();
            // 返す文章を入れる箱です。
            String message;
            // 返す HTTP ステータスを入れる箱です。
            int statusCode;
            // /hello のときの処理です。
            if (path.equals("/hello")) {
                // URL のクエリをそのまま取ります。
                String query = exchange.getRequestURI().getRawQuery();
                // 確認のために今の query を表示します。
                System.out.println("query = " + query);
                // いったんゲストにしておきます。
                String name = "ゲスト";
                // query があるときだけ、name= の後ろを切り出します。
                if (query != null) {
                    name = URLDecoder.decode(query.substring("name=".length()), StandardCharsets.UTF_8);
                }
                // 取り出した名前で挨拶を作ります。
                message = "こんにちは、" + name + "さん！";
                // 成功として返します。
                statusCode = 200;
            } else if (path.equals("/menu")) {
                // 今日の定食を返します。
                message = "今日の定食はカレー";
                // 成功として返します。
                statusCode = 200;
            } else if (path.equals("/bye")) {
                // さようならを返します。
                message = "さようなら！";
                // 成功として返します。
                statusCode = 200;
            } else {
                // 見つからないときの文を返します。
                message = "ページが見つかりません";
                // 見つからないときの番号を返します。
                statusCode = 404;
            }
            // 文字の文章をバイト列に変えます。
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // 返事の種類を設定します。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            // ステータスと本文の長さを伝えます。
            exchange.sendResponseHeaders(statusCode, body.length);
            // 返事の本文を書き込みます。
            try (OutputStream os = exchange.getResponseBody()) {
                // 本文を送ります。
                os.write(body);
            }
        });
        // サーバーを動かし始めます。
        server.start();
        // プログラムをすぐ終わらせないように止めておきます。
        Thread.currentThread().join();
    }
}
