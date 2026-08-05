// リクエストハンドラで使う HTTP Exchange 型を読み込む。
import com.sun.net.httpserver.HttpExchange;
// 軽量な HTTP サーバー実装を読み込む。
import com.sun.net.httpserver.HttpServer;
// レスポンス本文に使う出力ストリーム型を読み込む。
import java.io.OutputStream;
// サーバーの待ち受け先として使うソケットアドレス型を読み込む。
import java.net.InetSocketAddress;
// クエリ文字列をデコードする URL デコーダーを読み込む。
import java.net.URLDecoder;
// エンコードとデコードに使う UTF-8 の定義を読み込む。
import java.nio.charset.StandardCharsets;
// 動的配列の実装を読み込む。
import java.util.ArrayList;
// リスト初期化に使う配列ヘルパーを読み込む。
import java.util.Arrays;
// Todo 一覧で使う List インターフェースを読み込む。
import java.util.List;

// アプリケーションのエントリーポイントを定義する。
public class App {
    // main メソッドを定義する。
    public static void main(String[] args) throws Exception {
        // 8080 番ポートで HTTP サーバーを作成する。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ルートのリクエストハンドラを登録する。
        server.createContext("/", exchange -> {
            // リクエストパスを取得する。
            String path = exchange.getRequestURI().getPath();
            // レスポンス本文を保持する。
            String message;
            // レスポンスのステータスコードを保持する。
            int statusCode;
            // 既定のレスポンス形式をプレーンテキストにする。
            String contentType = "text/plain; charset=UTF-8";

            // /hello ルートを処理する。
            if (path.equals("/hello")) {
                // 生のクエリ文字列を取得する。
                String query = exchange.getRequestURI().getRawQuery();
                // デバッグ用にクエリ文字列を出力する。
                System.out.println("query = " + query);
                // 既定のゲスト名を設定する。
                String name = "ゲスト";
                // name パラメータがある場合はデコードする。
                if (query != null && query.startsWith("name=")) {
                    // name パラメータを取り出してデコードする。
                    name = URLDecoder.decode(query.substring("name=".length()), StandardCharsets.UTF_8);
                }
                // 挨拶メッセージを組み立てる。
                message = "こんにちは、" + name + "さん。";
                // レスポンスを成功扱いにする。
                statusCode = 200;
            // /menu ルートを処理する。
            } else if (path.equals("/menu")) {
                // メニューの応答文を作る。
                message = "今日のメニューはカレー";
                // レスポンスを成功扱いにする。
                statusCode = 200;
            // /bye ルートを処理する。
            } else if (path.equals("/bye")) {
                // お別れメッセージを作る。
                message = "さようなら";
                // レスポンスを成功扱いにする。
                statusCode = 200;
            // /todos ルートを処理する。
            } else if (path.equals("/todos")) {
                // サンプル項目から Todo 一覧を作成する。
                List<String> todos = new ArrayList<>(Arrays.asList("牛乳を買う", "宿題をする", "パンを焼く"));
                // Todo を 1 件追加する。
                todos.add("自分だけのTodo");
                // HTML のリストマークアップを組み立て始める。
                String html = "<ul>";
                // 各 Todo を list item として追加する。
                for (String todo : todos) {
                    // 現在の Todo を連結する。
                    html += "<li>" + todo + "</li>";
                }
                // 箇条書きを閉じる。
                html += "</ul>";
                // 生成した HTML をレスポンス本文に使う。
                message = html;
                // レスポンスを成功扱いにする。
                statusCode = 200;
                // このルートでは Content-Type を HTML にする。
                contentType = "text/html; charset=UTF-8";
            // それ以外のパスを 404 として処理する。
            } else {
                // 見つからない場合の応答文を作る。
                message = "ページが見つかりません";
                // レスポンスを 404 扱いにする。
                statusCode = 404;
            }

            // レスポンス文字列を UTF-8 バイト列に変換する。
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // レスポンスの Content-Type ヘッダーを設定する。
            exchange.getResponseHeaders().set("Content-Type", contentType);
            // HTTP ステータスと本文サイズを送信する。
            exchange.sendResponseHeaders(statusCode, body.length);
            // 本文を書き込み、レスポンスストリームを閉じる。
            try (OutputStream os = exchange.getResponseBody()) {
                // 本文全体を書き込む。
                os.write(body);
            }
        });

        // サーバーを起動する。
        server.start();
        // アプリケーションスレッドを待機し続ける。
        Thread.currentThread().join();
    }
}
