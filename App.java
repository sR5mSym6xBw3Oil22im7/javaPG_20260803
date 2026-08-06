
// HTTP サーバーを使うための型を読み込む
import com.sun.net.httpserver.HttpExchange;
// HTTP サーバー本体を使うための型を読み込む
import com.sun.net.httpserver.HttpServer;
// 画面に返す文字列を書き出すための型を読み込む
import java.io.OutputStream;
// サーバーの待ち受け先を作るための型を読み込む
import java.net.InetSocketAddress;
// フォームの文字を正しく読み直すための型を読み込む
import java.net.URLDecoder;
// 文字コードをそろえるための定数を読み込む
import java.nio.charset.StandardCharsets;
// Todo を入れる一覧を使うための型を読み込む
import java.util.ArrayList;
// Todo を入れる一覧の型を読み込む
import java.util.List;

// アプリ本体のクラスを定義する
public class App {
    // アプリの開始点を定義する
    public static void main(String[] args) throws Exception {
        // Todo をためる一覧を一度だけ作る
        List<String> todos = new ArrayList<>();
        // 8080 番ポートで HTTP サーバーを作る
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ルートの受け皿を作る
        server.createContext("/", exchange -> {
            // リクエストのパスを取り出す
            String path = exchange.getRequestURI().getPath();
            // レスポンスの本文を入れる変数を用意する
            String message;
            // レスポンスの状態コードを入れる変数を用意する
            int statusCode;
            // 画面の種類を入れる変数を用意する
            String contentType = "text/plain; charset=UTF-8";

            // /hello を処理する
            if (path.equals("/hello")) {
                // 受け取ったクエリ文字列を取り出す
                String query = exchange.getRequestURI().getRawQuery();
                // デバッグ用にクエリ文字列を表示する
                System.out.println("query = " + query);
                // 名前の初期値を入れる
                String name = "ゲスト";
                // name という値があれば取り出す
                if (query != null && query.startsWith("name=")) {
                    // URL で送られた名前を文字に戻す
                    name = URLDecoder.decode(query.substring("name=".length()), StandardCharsets.UTF_8);
                }
                // あいさつ文を作る
                message = "こんにちは、" + name + "さん。";
                // 成功の状態コードにする
                statusCode = 200;
                // /menu を処理する
            } else if (path.equals("/menu")) {
                // メニューの文を返す
                message = "今日のメニューはカレー";
                // 成功の状態コードにする
                statusCode = 200;
                // /bye を処理する
            } else if (path.equals("/bye")) {
                // おわかれの文を返す
                message = "さようなら";
                // 成功の状態コードにする
                statusCode = 200;
                // / は Todo の一覧と入力フォームを返す
            } else if (path.equals("/")) {
                // HTML を作るための箱を用意する
                StringBuilder html = new StringBuilder();
                // HTML の先頭を入れる
                html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Todo</title></head><body>");
                // 入力フォームを入れる
                html.append("<form method='post' action='/add'>");
                // 入力欄を入れる
                html.append("<input type='text' name='todo'>");
                // 送信ボタンを入れる
                html.append("<button type='submit'>追加</button>");
                // 入力フォームを閉じる
                html.append("</form>");
                // 一覧の先頭を入れる
                html.append("<ul>");
                // Todo を 1 件ずつ並べる
                for (String todo : todos) {
                    // 1 件分の表示を入れる
                    html.append("<li>").append(todo).append("</li>");
                }
                // 一覧を閉じる
                html.append("</ul>");
                // HTML の終わりを入れる
                html.append("</body></html>");
                // HTML 全体を本文にする
                message = html.toString();
                // 成功の状態コードにする
                statusCode = 200;
                // 画面の種類を HTML にする
                contentType = "text/html; charset=UTF-8";
                // それ以外は 404 にする
            } else {
                // 見つからない文を返す
                message = "ページが見つかりません";
                // 見つからない状態コードにする
                statusCode = 404;
            }

            // 本文を UTF-8 のバイト列にする
            byte[] body = message.getBytes(StandardCharsets.UTF_8);
            // 返す文字の種類を伝える
            exchange.getResponseHeaders().set("Content-Type", contentType);
            // 状態コードと本文の長さを送る
            exchange.sendResponseHeaders(statusCode, body.length);
            // 返す本文の流れを開く
            try (OutputStream os = exchange.getResponseBody()) {
                // 本文を書き込む
                os.write(body);
            }
        });

        // /add を受け取る受け皿を作る
        server.createContext("/add", exchange -> {
            // POST 以外なら受け付けない
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // 方法が違うことを返す
                exchange.sendResponseHeaders(405, -1);
                // ここで処理を終える
                return;
            }
            // フォームの中身を文字で読む
            String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            // todo の値を入れる箱を用意する
            String todo = "";
            // 送られてきた項目を 1 つずつ見る
            for (String part : form.split("&")) {
                // todo の項目かどうかを調べる
                if (part.startsWith("todo=")) {
                    // URL で送られた値を文字に戻す
                    todo = URLDecoder.decode(part.substring("todo=".length()), StandardCharsets.UTF_8);
                    // もう見つかったので終わる
                    break;
                }
            }
            // 空でないときだけ追加する
            if (!todo.isEmpty()) {
                // Todo を一覧に追加する
                todos.add(todo);
            }
            // もとの画面に戻すための場所を伝える
            exchange.getResponseHeaders().set("Location", "/");
            // 303 で / に戻す
            exchange.sendResponseHeaders(303, -1);
            // 返す本文の流れを閉じる
            exchange.close();
        });

        // サーバーを動かし始める
        server.start();
        // メインスレッドを止めてサーバーを生かしておく
        Thread.currentThread().join();
    }
}
