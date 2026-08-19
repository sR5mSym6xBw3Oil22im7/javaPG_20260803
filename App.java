
// HTTPサーバーを利用するためのクラスを読み込む
import com.sun.net.httpserver.HttpServer;
// ファイル保存時のエラーを扱うクラスを読み込む
import java.io.IOException;
// サーバーの待ち受けアドレスを扱うクラスを読み込む
import java.net.InetSocketAddress;
// URLエンコードされた文字列を復号するクラスを読み込む
import java.net.URLDecoder;
// 文字コードを指定するクラスを読み込む
import java.nio.charset.StandardCharsets;
// ファイルを読み書きするクラスを読み込む
import java.nio.file.Files;
// ファイルの場所を表すクラスを読み込む
import java.nio.file.Path;
// 可変長リストを扱うクラスを読み込む
import java.util.ArrayList;
// リストのインターフェースを読み込む
import java.util.List;

// Todoアプリケーションの処理を定義する
public class App {
    // ★変更 List と、次に振る番号を main の外に置く
    static List<Todo> todos = new ArrayList<>();
    // ★変更 次に振る番号は 1 から始める
    static int nextId = 1;

    // アプリケーションを起動する
    public static void main(String[] args) throws Exception {
        // ★変更 起動時に保存済みのTodoを読み込む
        load();

        // 8080番ポートでHTTPサーバーを作成する
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // ルートパスへのリクエスト処理を登録する
        server.createContext("/", exchange -> {
            // リクエストされたパスを取得する
            String path = exchange.getRequestURI().getPath();
            // レスポンス本文を保持する変数を用意する
            String message;
            // リクエストメソッドを取得する
            String method = exchange.getRequestMethod();
            // 標準のレスポンス文字コードを設定する
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

            // 追加リクエストかどうかを判定する
            if (path.equals("/add") && method.equals("POST")) {
                // POSTされた本文をUTF-8文字列として読み込む
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // 本文からtodo=の後ろを取り出す
                String value = body.substring(5);
                // TodoのタイトルをURLデコードする
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                // タイトルが空でない場合だけTodoを追加する
                if (!title.isEmpty()) {
                    // ★変更 フォームの内容からTodoを1件作ってListに追加する
                    todos.add(new Todo(nextId, title));
                    // ★変更 次のTodoに使う番号を進める
                    nextId++;
                    // ★変更 Todoを追加した一覧をファイルに保存する
                    save();
                    // Todo追加処理の条件分岐を終了する
                }
                // 一覧ページへのリダイレクト先を設定する
                exchange.getResponseHeaders().set("Location", "/");
                // リダイレクトレスポンスを送信する
                exchange.sendResponseHeaders(303, -1);
                // リクエストを閉じる
                exchange.close();
                // 追加処理を終了する
                return;
                // 完了または削除リクエストを処理する
                // 完了または削除のGETリクエストかどうかを判定する
            } else if ((path.equals("/done") || path.equals("/delete")) && method.equals("GET")) {
                // ★追加 query から id を取り出す
                Integer id = parseId(exchange.getRequestURI().getQuery());
                // ★追加 id があるときだけ Todo を変える
                // idが取得できた場合だけ操作する
                if (id != null) {
                    // ★追加 /done なら完了にする
                    // 完了操作かどうかを判定する
                    if (path.equals("/done")) {
                        // ★変更 idが一致したTodoを探す
                        Todo todo = findTodoById(id);
                        // ★変更 見つかったTodoの完了状態を反転する
                        if (todo != null) {
                            // ★変更 Todoの完了状態を反転して切り替える
                            todo.setDone(!todo.isDone());
                            // ★変更 完了状態を変更した一覧をファイルに保存する
                            save();
                        // ★変更 Todoが見つかった場合の処理を終了する
                        }
                        // 完了操作でない場合は削除操作を行う
                    } else {
                        // ★変更 idが一致したTodoを探す
                        Todo todo = findTodoById(id);
                        // ★変更 見つかったTodoをListから削除する
                        if (todo != null) {
                            // ★変更 Todoを一覧から削除する
                            todos.remove(todo);
                            // ★変更 Todoを削除した一覧をファイルに保存する
                            save();
                        // ★変更 Todoが見つかった場合の処理を終了する
                        }
                        // 完了または削除の分岐を終了する
                    }
                    // idの有無による条件分岐を終了する
                }
                // ★追加 どちらの操作でも "/" に戻す
                exchange.getResponseHeaders().set("Location", "/");
                // ★追加 303 で "/" に戻す
                exchange.sendResponseHeaders(303, -1);
                // ★追加 ここで処理を終える
                exchange.close();
                // ★追加 一覧表示の処理へ進まない
                return;
                // 一覧ページを表示する
                // 一覧ページのリクエストかどうかを判定する
            } else if (path.equals("/")) {
                // 一覧ページの先頭HTMLを作成する
                String html = "<head><style>body{max-width:600px;margin:20px auto;padding:0 16px;font-size:16px}</style></head><body>"
                        // ページの見出しを表示する
                        + "<h1>わたしのTodo</h1>"
                // Todo入力フォームをHTMLに追加する
                        + "<form method='post' action='/add'>"
                // Todo入力フォームをHTMLに追加する
                        + "<input name='todo'><button>追加</button>"
                // 入力フォームの終了タグをHTMLに追加する
                        + "</form>";
                // Todoが0件のときだけ空一覧のメッセージを追加する
                if (todos.isEmpty()) {
                    // 空一覧のメッセージを表示する
                    html += "<p>やることは、いまゼロです</p>";
                }
                // Todo一覧の開始タグをHTMLに追加する
                html += "<ul>";
                // ★変更 Todo の title を表示し、done のときだけ印を付ける
                // ★追加 各 Todo の横に完了と削除のリンクを付ける
                // Todo一覧を順番にHTMLへ追加する
                for (Todo todo : todos) {
                    // 未完了Todoの表示記号を空にする
                    String mark = "";
                    // Todoが完了済みか確認する
                    // Todoが完了済みかどうかを判定する
                    if (todo.isDone()) {
                        // 完了済みTodoの表示記号を設定する
                        mark = " ✔";
                        // 完了状態の条件分岐を終了する
                    }
                    // ★追加 id つきのリンクを Todo の横に表示する
                    html += "<li>" + todo.getTitle() + mark + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                    // Todo一覧の繰り返しを終了する
                }
                // Todo一覧の終了タグとページの終了タグをHTMLに追加する
                html += "</ul></body>";
                // 作成したHTMLをレスポンス本文に設定する
                message = html;
                // HTMLレスポンスの文字コードを設定する
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                // どの既知のパスにも一致しない場合を処理する
            } else {
                // ★変更 未使用の /hello・/bye ルーティングを削除
                // 見つからないパスへのメッセージを設定する
                message = "ページが見つかりません";
                // パスごとの条件分岐を終了する
            }
            // レスポンス本文をUTF-8のバイト列に変換する
            byte[] responseBody = message.getBytes(StandardCharsets.UTF_8);
            // 成功レスポンスのヘッダーを送信する
            exchange.sendResponseHeaders(200, responseBody.length);
            // レスポンス本文を書き込む
            exchange.getResponseBody().write(responseBody);
            // レスポンス本文を閉じる
            exchange.getResponseBody().close();
            // リクエスト処理の登録を終了する
        });

        // HTTPサーバーの待ち受けを開始する
        server.start();
        // 起動したサーバーのURLを表示する
        System.out.println("サーバー起動: http://localhost:8080 (止めるときは Ctrl+C)");
    // mainメソッドを終了する
    }

    // ★追加 Todo一覧をtodos.csvへUTF-8で保存する
    static void save() {
        // ★追加 ファイル保存時のエラーを処理する
        try {
            // ★追加 CSV形式の本文を作る
            StringBuilder csv = new StringBuilder();
            // ★追加 Todo一覧を1件ずつCSV本文に追加する
            for (Todo todo : todos) {
                // ★追加 id、完了状態、タイトルの順で1行を追加する
                csv.append(todo.getId()).append(",")
                        .append(todo.isDone() ? "1" : "0").append(",")
                        .append(todo.getTitle()).append(System.lineSeparator());
            }
            // ★追加 Todo一覧をUTF-8のtodos.csvへ書き出す
            Files.writeString(Path.of("todos.csv"), csv.toString(), StandardCharsets.UTF_8);
        // ★追加 ファイル保存に失敗した場合を処理する
        } catch (IOException e) {
            // ★追加 保存失敗を実行時エラーとして知らせる
            throw new RuntimeException("todos.csvの保存に失敗しました", e);
        // ★追加 ファイル保存処理を終了する
        }
    // ★追加 saveメソッドを終了する
    }

    // ★追加 todos.csvからTodo一覧を読み込む
    static void load() {
        // ★追加 ファイル読み込み時のエラーを処理する
        try {
            // ★追加 起動時の一覧を空にする
            todos.clear();
            // ★追加 Todo番号を初期値に戻す
            nextId = 1;
            // ★追加 保存ファイルの場所を指定する
            Path file = Path.of("todos.csv");
            // ★追加 保存ファイルがなければ空の一覧のまま終了する
            if (!Files.exists(file)) {
                // ★追加 保存ファイルがない場合の読み込みを終了する
                return;
            }
            // ★追加 CSVファイルをUTF-8で1行ずつ読み込む
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                // ★追加 空行を読み飛ばす
                if (line.isEmpty()) {
                    // ★追加 空行の処理を終了する
                    continue;
                }
                // ★追加 id、完了状態、タイトルの3項目に分ける
                String[] fields = line.split(",", 3);
                // ★追加 項目数が3つでない行を読み飛ばす
                if (fields.length != 3) {
                    // ★追加 不正な行の処理を終了する
                    continue;
                }
                // ★追加 文字列のidを整数に変換する
                int id = Integer.parseInt(fields[0]);
                // ★追加 1を完了済み、0を未完了として読み込む
                boolean done = fields[1].equals("1");
                // ★追加 読み込んだ内容でTodoを作成する
                Todo todo = new Todo(id, fields[2]);
                // ★追加 読み込んだ完了状態をTodoに設定する
                todo.setDone(done);
                // ★追加 読み込んだTodoを一覧に追加する
                todos.add(todo);
                // ★追加 最大idの次の番号をnextIdに設定する
                if (id >= nextId) {
                    // ★追加 重複しない次のTodo番号を設定する
                    nextId = id + 1;
                // ★追加 最大id判定の条件分岐を終了する
                }
            // ★追加 CSV行の読み込みを終了する
            }
        // ★追加 ファイル読み込みに失敗した場合を処理する
        } catch (IOException | NumberFormatException e) {
            // ★追加 読み込み失敗を実行時エラーとして知らせる
            throw new RuntimeException("todos.csvの読み込みに失敗しました", e);
        // ★追加 ファイル読み込み処理を終了する
        }
    // ★追加 loadメソッドを終了する
    }

    // ★追加 idからTodoを1件探す
    static Todo findTodoById(int id) {
        // ★追加 Todo一覧を先頭から検索する
        for (Todo todo : todos) {
            // ★追加 Todoのidが指定値と一致するか判定する
            if (todo.getId() == id) {
                // ★追加 一致したTodoを返す
                return todo;
            // ★追加 id一致の条件分岐を終了する
            }
        // ★追加 Todo検索の繰り返しを終了する
        }
        // ★追加 見つからなかったことをnullで示す
        return null;
    // ★追加 findTodoByIdメソッドを終了する
    }

    // ★追加 query 文字列から id を読む
    static Integer parseId(String query) {
        // ★追加 query がないときは失敗にする
        // queryがない場合は失敗として扱う
        if (query == null || query.isEmpty()) {
            // 不正なqueryを示すnullを返す
            return null;
            // query有無の条件分岐を終了する
        }
        // ★追加 & で分かれた部分を順に見る
        // queryの各パラメーターを順番に確認する
        for (String part : query.split("&")) {
            // ★追加 id= で始まる部分だけ使う
            // idパラメーターかどうかを判定する
            if (part.startsWith("id=")) {
                // ★追加 id= の後ろを数値に変える
                // idの数値変換を試みる
                try {
                    // 文字列のidを整数に変換して返す
                    return Integer.parseInt(part.substring(3));
                    // 数値変換に失敗した場合を処理する
                } catch (NumberFormatException e) {
                    // 数値でないidを示すnullを返す
                    return null;
                    // id変換処理を終了する
                }
                // idパラメーターの条件分岐を終了する
            }
            // queryパラメーターの繰り返しを終了する
        }
        // ★追加 id がなければ失敗にする
        // idが見つからなければ失敗にする
        return null;
        // parseIdメソッドを終了する
    }
    // Appクラスの定義を終了する
}

// ★変更 Todo を表すクラスを追加
class Todo {
    // Todoを識別する番号を保持する
    private final int id;
    // Todoのタイトルを保持する
    private final String title;
    // Todoの完了状態を保持する
    private boolean done;

    // ★変更 Todo は done=false で初期化する
    // Todoの初期値を設定する
    Todo(int id, String title) {
        // Todoのidを設定する
        this.id = id;
        // Todoのタイトルを設定する
        this.title = title;
        // Todoを未完了状態で初期化する
        this.done = false;
        // Todoコンストラクターを終了する
    }

    // ★変更 id を読み出すメソッド
    // idを返すメソッドを定義する
    int getId() {
        // 保持しているidを返す
        return id;
        // id取得メソッドを終了する
    }

    // ★変更 title を読み出すメソッド
    // titleを返すメソッドを定義する
    String getTitle() {
        // 保持しているtitleを返す
        return title;
        // title取得メソッドを終了する
    }

    // ★変更 done を読み出すメソッド
    // 完了状態を返すメソッドを定義する
    boolean isDone() {
        // 保持している完了状態を返す
        return done;
        // 完了状態取得メソッドを終了する
    }

    // ★変更 done を書き換えるメソッド
    // 完了状態を設定するメソッドを定義する
    void setDone(boolean done) {
        // 受け取った完了状態を保存する
        this.done = done;
        // 完了状態設定メソッドを終了する
    }
    // Todoクラスの定義を終了する
}
