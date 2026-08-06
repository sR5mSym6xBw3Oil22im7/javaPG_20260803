
// ★変更 HTTP サーバーを使うための読み込み
import com.sun.net.httpserver.HttpExchange;
// ★変更 HTTP サーバー本体を使うための読み込み
import com.sun.net.httpserver.HttpServer;
// ★変更 画面に返す文字をバイト列へ直すための読み込み
import java.io.OutputStream;
// ★変更 入出力のエラーを表すための読み込み
import java.io.IOException;
// ★変更 サーバーの待ち受け先を作るための読み込み
import java.net.InetSocketAddress;
// ★変更 フォームの文字を元に戻すための読み込み
import java.net.URLDecoder;
// ★変更 文字コードをそろえるための読み込み
import java.nio.charset.StandardCharsets;
// ★変更 Todo をためる箱を作るための読み込み
import java.util.ArrayList;
// ★変更 Todo をたくさん持つための読み込み
import java.util.List;

// ★変更 Todo サーバーの本体クラス
public class App {
    // ★変更 Todo をためる一覧をクラス直下に置く
    private static final List<Todo> todos = new ArrayList<>();
    // ★変更 次に付ける id をクラス直下に置く
    private static int nextId = 1;

    // ★変更 アプリの起動点を用意する
    public static void main(String[] args) throws Exception {
        // ★変更 動作確認用の Todo を 1 件目として入れる
        addTodo("\u725b\u4e73\u3092\u8cb7\u3046", false);
        // ★変更 動作確認用の Todo を 2 件目として入れる
        addTodo("\u5375\u3092\u8cb7\u3046", true);
        // ★変更 8080 番で待ち受けるサーバーを作る
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // ★変更 / の画面を返す処理を登録する
        server.createContext("/", App::handleRoot);
        // ★変更 /add の処理を登録する
        server.createContext("/add", App::handleAdd);
        // ★変更 参考用の文字を返す /hello を登録する
        server.createContext("/hello", App::handleHello);
        // ★変更 参考用の文字を返す /menu を登録する
        server.createContext("/menu", App::handleMenu);
        // ★変更 参考用の文字を返す /bye を登録する
        server.createContext("/bye", App::handleBye);
        // ★変更 サーバーを動かし始める
        server.start();
        // ★変更 メインの流れを止めてサーバーを生かしておく
        Thread.currentThread().join();
    }

    // ★変更 / の画面を作る
    private static void handleRoot(HttpExchange exchange) throws IOException {
        // ★変更 返す HTML を入れる箱を作る
        StringBuilder html = new StringBuilder();
        // ★変更 HTML の先頭を書く
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Todo</title></head><body>");
        // ★変更 入力フォームを開く
        html.append("<form method='post' action='/add'>");
        // ★変更 Todo の文字を入れる入力欄を作る
        html.append("<input type='text' name='todo'>");
        // ★変更 送信ボタンを作る
        html.append("<button type='submit'>\u8ffd\u52a0</button>");
        // ★変更 入力フォームを閉じる
        html.append("</form>");
        // ★変更 一覧の入れ物を開く
        html.append("<ul>");
        // ★変更 Todo を 1 件ずつ並べる
        for (Todo todo : todos) {
            // ★変更 1 件ぶんの li を開く
            html.append("<li>");
            // ★変更 Todo の title を表示する
            html.append(todo.getTitle());
            // ★変更 終わった Todo にだけ印を付ける
            if (todo.isDone()) {
                // ★変更 終わった印を足す
                html.append(" \u2714");
            }
            // ★変更 1 件ぶんの li を閉じる
            html.append("</li>");
        }
        // ★変更 一覧の入れ物を閉じる
        html.append("</ul>");
        // ★変更 HTML の終わりを書く
        html.append("</body></html>");
        // ★変更 返す本文を文字列にする
        String message = html.toString();
        // ★変更 文字を UTF-8 のバイト列に変える
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        // ★変更 HTML として返すことを伝える
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        // ★変更 200 で本文の長さぶん返す
        exchange.sendResponseHeaders(200, body.length);
        // ★変更 返す本文を書き込む
        try (OutputStream os = exchange.getResponseBody()) {
            // ★変更 本文のバイト列を送る
            os.write(body);
        }
    }

    // ★変更 /add の送信内容を受け取る
    private static void handleAdd(HttpExchange exchange) throws IOException {
        // ★変更 POST 以外なら受け付けない
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // ★変更 POST 以外はエラーにする
            exchange.sendResponseHeaders(405, -1);
            // ★変更 ここで処理を終える
            exchange.close();
            // ★変更 早めに戻る
            return;
        }
        // ★変更 フォームの中身を文字で読む
        String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        // ★変更 todo の入力文字を入れる箱を作る
        String todo = "";
        // ★変更 送られてきた項目を 1 つずつ見る
        for (String part : form.split("&")) {
            // ★変更 todo の項目かどうかを調べる
            if (part.startsWith("todo=")) {
                // ★変更 URL で変わった文字を元に戻す
                todo = URLDecoder.decode(part.substring("todo=".length()), StandardCharsets.UTF_8);
                // ★変更 見つかったので繰り返しを終える
                break;
            }
        }
        // ★変更 文字が空でなければ Todo を追加する
        if (!todo.isEmpty()) {
            // ★変更 新しい Todo を 1 件作って保存する
            addTodo(todo, false);
        }
        // ★変更 一覧へ戻るように場所を知らせる
        exchange.getResponseHeaders().set("Location", "/");
        // ★変更 追加後は一覧へ戻す
        exchange.sendResponseHeaders(303, -1);
        // ★変更 使い終わった接続を閉じる
        exchange.close();
    }

    // ★変更 /hello の文字を返す
    private static void handleHello(HttpExchange exchange) throws IOException {
        // ★変更 参考メッセージを作る
        sendText(exchange, 200, "\u3053\u3093\u306b\u3061\u306f");
    }

    // ★変更 /menu の文字を返す
    private static void handleMenu(HttpExchange exchange) throws IOException {
        // ★変更 参考メッセージを作る
        sendText(exchange, 200, "\u4eca\u65e5\u306e\u30e1\u30cb\u30e5\u30fc\u306f\u30ab\u30ec\u30fc");
    }

    // ★変更 /bye の文字を返す
    private static void handleBye(HttpExchange exchange) throws IOException {
        // ★変更 参考メッセージを作る
        sendText(exchange, 200, "\u3055\u3088\u3046\u306a\u3089");
    }

    // ★変更 文字をそのまま返す共通処理を作る
    private static void sendText(HttpExchange exchange, int statusCode, String message) throws IOException {
        // ★変更 返す文字を UTF-8 のバイト列にする
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        // ★変更 文字として返すことを伝える
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        // ★変更 指定された状態で返す
        exchange.sendResponseHeaders(statusCode, body.length);
        // ★変更 本文を書き込む
        try (OutputStream os = exchange.getResponseBody()) {
            // ★変更 文字の中身を送る
            os.write(body);
        }
    }

    // ★変更 Todo を 1 件追加する共通処理を作る
    private static void addTodo(String title, boolean done) {
        // ★変更 新しい Todo を id 付きで作る
        Todo todo = new Todo(nextId, title, done);
        // ★変更 次の id を 1 進める
        nextId++;
        // ★変更 作った Todo を一覧に入れる
        todos.add(todo);
    }
}

// ★変更 Todo という 1 件分のデータを表す
class Todo {
    // ★変更 何番かを入れる
    private final int id;
    // ★変更 やることの名前を入れる
    private final String title;
    // ★変更 終わったかどうかを入れる
    private boolean done;

    // ★変更 Todo を作る
    Todo(int id, String title, boolean done) {
        // ★変更 id を覚える
        this.id = id;
        // ★変更 title を覚える
        this.title = title;
        // ★変更 done を覚える
        this.done = done;
    }

    // ★変更 id を読み出す
    public int getId() {
        // ★変更 id を返す
        return id;
    }

    // ★変更 title を読み出す
    public String getTitle() {
        // ★変更 title を返す
        return title;
    }

    // ★変更 done を読み出す
    public boolean isDone() {
        // ★変更 done を返す
        return done;
    }

    // ★変更 done を書き換える
    public void setDone(boolean done) {
        // ★変更 新しい done を入れる
        this.done = done;
    }
}
