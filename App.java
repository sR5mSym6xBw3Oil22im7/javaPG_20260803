
// HTTPサーバーを利用するためのクラスを読み込む
import com.sun.net.httpserver.HttpServer;
// サーバーの待ち受けアドレスを扱うクラスを読み込む
import java.net.InetSocketAddress;
// URLエンコードされた文字列を復号するクラスを読み込む
import java.net.URLDecoder;
// 文字コードを指定するクラスを読み込む
import java.nio.charset.StandardCharsets;
// CSSファイルを読み込むクラス
import java.nio.file.Files;
// ファイルの場所を扱うクラス
import java.nio.file.Path;
// 可変長リストを扱うクラスを読み込む
import java.util.ArrayList;
// リストのインターフェースを読み込む
import java.util.List;
// ★変更 データベース接続を扱うクラスを読み込む
import java.sql.Connection;
// ★変更 データベース接続を作成するクラスを読み込む
import java.sql.DriverManager;
// ★変更 SQL実行時のエラーを扱うクラスを読み込む
import java.sql.SQLException;
// ★変更 パラメーター付きSQLを扱うクラスを読み込む
import java.sql.PreparedStatement;
// ★変更 SELECT結果を扱うクラスを読み込む
import java.sql.ResultSet;

// Todoアプリケーションの処理を定義する
public class App {
    // ★変更 SQLiteデータベースの接続先を指定する
    static final String DB_URL = "jdbc:sqlite:todos.db";

    // アプリケーションを起動する
    public static void main(String[] args) throws Exception {
        // ★変更 起動時にSQLiteのtodos表を準備する
        initializeDatabase();

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
                    // ★変更 フォームの内容をSQLiteへINSERTする
                    insertTodo(title);
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
                        // ★変更 指定したTodoの完了状態をSQLiteでUPDATEする
                        updateTodo(id);
                        // 完了操作でない場合は削除操作を行う
                    } else {
                        // ★追加 Todoの状態に応じて取消線または削除を行う
                        int state = findTodoState(id);
                        // ★追加 完了済みで取消線済みなら一覧から削除する
                        if (state == 3) {
                            // ★変更 指定したTodoをSQLiteからDELETEする
                            deleteTodo(id);
                        // ★追加 完了済みで取消線がなければ取消線を付ける
                        } else if (state == 2) {
                            // ★追加 指定したTodoに削除待ち状態を保存する
                            markTodoForDeletion(id);
                        // ★追加 未完了Todoは従来どおり1回で削除する
                        } else {
                            // ★追加 指定したTodoをSQLiteからDELETEする
                            deleteTodo(id);
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
                // ★変更 SQLiteからSELECTしたTodo一覧を取得する
                List<Todo> todos = findAllTodos();
                // ★追加 完了済みTodoの件数を数える
                int completedCount = 0;
                // ★追加 Todo一覧から完了済みの件数を集計する
                for (Todo todo : todos) {
                    // ★追加 完了済みTodoだけ件数を増やす
                    if (todo.isDone()) {
                        // ★追加 完了件数を1件増やす
                        completedCount++;
                    }
                }
                // ★変更 添付画像を参考に一覧ページのモノトーンCSSを設定する
                // ★変更 外部CSSを読み込む一覧ページのHTMLを作成する
                String html = "<head><meta name='viewport' content='width=device-width, initial-scale=1'><link rel='stylesheet' href='/style.css'></head><body>"
                        // ページの見出しを表示する
                        + "<h1>わたしのTodo</h1>"
                // Todo入力フォームをHTMLに追加する
                        + "<form method='post' action='/add'>"
                // Todo入力フォームをHTMLに追加する
                        + "<input name='todo'><button>追加</button>"
                // 入力フォームの終了タグをHTMLに追加する
                        + "</form>";
                // ★追加 Todo全件数と完了件数を表示する
                html += "<p class='summary'>" + todos.size() + "件中" + completedCount + "件 完了</p>";
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
                    // ★追加 表示するTodoタイトルを用意する
                    String title = "<span>" + todo.getTitle() + "</span>";
                    // ★追加 完了済みで削除待ちのTodoだけに取消線を付ける
                    if (todo.isDone() && todo.isDeleteMarked()) {
                        // ★追加 Todoタイトルを取消線付きのHTMLで包む
                        title = "<span style='text-decoration:line-through'>" + title + "</span>";
                    }
                    // ★追加 Todo削除リンクの基本部分を作成する
                    String deleteLink = "<a href='/delete?id=" + todo.getId() + "'>削除</a>";
                    // ★変更 未完了または取消線付きTodoに削除確認を表示する
                    if (!todo.isDone() || todo.isDeleteMarked()) {
                    // ★追加 削除確認を許可した場合だけリンクを実行する
                        deleteLink = "<a href='/delete?id=" + todo.getId()
                                + "' onclick=\"return confirm('削除してよいですか？');\">削除</a>";
                    }
                    // ★追加 完了状態に応じたTodo行のCSSクラスを用意する
                    String todoClass = todo.isDone() ? "todo done" : "todo";
                    // ★変更 id つきのリンクを Todo の横に表示する
                    html += "<li class='" + todoClass + "'>" + title + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> " + deleteLink + "</li>";
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

        // CSSファイルへのリクエスト処理を登録する
        server.createContext("/style.css", exchange -> {
            // CSSファイルをUTF-8のバイト列として読み込む
            byte[] responseBody = Files.readAllBytes(Path.of("style.css"));
            // CSSレスポンスの文字コードを設定する
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            // CSSレスポンスのヘッダーを送信する
            exchange.sendResponseHeaders(200, responseBody.length);
            // CSSレスポンスの本文を書き込む
            exchange.getResponseBody().write(responseBody);
            // CSSレスポンスを閉じる
            exchange.getResponseBody().close();
        });

        // ★追加 JSON APIへのリクエスト処理を登録する
        server.createContext("/api/todos", exchange -> {
            // ★追加 リクエストメソッドがGETか確認する
            if (!exchange.getRequestMethod().equals("GET")) {
                // ★追加 GET以外のリクエストには405を返す
                exchange.sendResponseHeaders(405, -1);
                // ★追加 GET以外のレスポンスを閉じる
                exchange.close();
                // ★追加 GET以外の処理を終了する
                return;
            }
            // ★追加 SQLiteから取得したTodo一覧をJSON文字列に変換する
            String json = todosToJson();
            // ★追加 Content-Typeをapplication/jsonだけに設定する
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            // ★追加 JSON文字列をUTF-8のバイト列に変換する
            byte[] responseBody = json.getBytes(StandardCharsets.UTF_8);
            // ★追加 JSONレスポンスのヘッダーを送信する
            exchange.sendResponseHeaders(200, responseBody.length);
            // ★追加 JSONレスポンスの本文を書き込む
            exchange.getResponseBody().write(responseBody);
            // ★追加 JSONレスポンスを閉じる
            exchange.getResponseBody().close();
        // ★追加 JSON APIのリクエスト処理を終了する
        });

        // HTTPサーバーの待ち受けを開始する
        server.start();
        // 起動したサーバーのURLを表示する
        System.out.println("サーバー起動: http://localhost:8080 (止めるときは Ctrl+C)");
    // mainメソッドを終了する
    }

    // ★追加 SQLiteのtodos表を作成する
    static void initializeDatabase() {
        // ★追加 データベース接続とSQL文を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER, delete_marked INTEGER DEFAULT 0)")) {
            // ★追加 todos表を作成するSQLを実行する
            statement.executeUpdate();
        // ★追加 データベース処理のエラーを扱う
        } catch (SQLException e) {
            // ★追加 データベース初期化の失敗を知らせる
            throw new RuntimeException("SQLiteの初期化に失敗しました", e);
        // ★追加 データベース初期化処理を終了する
        }
        // ★追加 既存のtodos表にも削除待ち列を追加する
        try (Connection connection = DriverManager.getConnection(DB_URL);
             // ★追加 既存DB用の列追加SQLを準備する
             PreparedStatement statement = connection.prepareStatement(
                     "ALTER TABLE todos ADD COLUMN delete_marked INTEGER DEFAULT 0")) {
            // ★追加 既存DBへの列追加を実行する
            statement.executeUpdate();
        // ★追加 列がすでにある場合はそのまま利用する
        } catch (SQLException e) {
            // ★追加 既存列またはSQLiteの制約によるエラーを許容する
        // ★追加 既存DBへの列追加処理を終了する
        }
    // ★追加 initializeDatabaseメソッドを終了する
    }

    // ★追加 TodoをINSERTでSQLiteへ追加する
    static void insertTodo(String title) {
        // ★追加 INSERT文と接続を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO todos (title, done, delete_marked) VALUES (?, 0, 0)")) {
            // ★追加 TodoタイトルをSQLの値として設定する
            statement.setString(1, title);
            // ★追加 INSERT文を実行する
            statement.executeUpdate();
        // ★追加 Todo追加処理のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo追加の失敗を知らせる
            throw new RuntimeException("Todoの追加に失敗しました", e);
        // ★追加 Todo追加処理を終了する
        }
    // ★追加 insertTodoメソッドを終了する
    }

    // ★追加 Todoの完了状態をUPDATEで反転する
    static void updateTodo(int id) {
        // ★追加 UPDATE文と接続を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE todos SET done = CASE done WHEN 0 THEN 1 ELSE 0 END, delete_marked = 0 WHERE id = ?")) {
            // ★追加 更新対象のTodo番号をSQLの値として設定する
            statement.setInt(1, id);
            // ★追加 UPDATE文を実行する
            statement.executeUpdate();
        // ★追加 Todo更新処理のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo更新の失敗を知らせる
            throw new RuntimeException("Todoの更新に失敗しました", e);
        // ★追加 Todo更新処理を終了する
        }
    // ★追加 updateTodoメソッドを終了する
    }

    // ★追加 TodoをDELETEでSQLiteから削除する
    static void deleteTodo(int id) {
        // ★追加 DELETE文と接続を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM todos WHERE id = ?")) {
            // ★追加 削除対象のTodo番号をSQLの値として設定する
            statement.setInt(1, id);
            // ★追加 DELETE文を実行する
            statement.executeUpdate();
        // ★追加 Todo削除処理のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo削除の失敗を知らせる
            throw new RuntimeException("Todoの削除に失敗しました", e);
        // ★追加 Todo削除処理を終了する
        }
    // ★追加 deleteTodoメソッドを終了する
    }

    // ★追加 Todoを削除待ち状態にする
    static void markTodoForDeletion(int id) {
        // ★追加 UPDATE文と接続を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             // ★追加 削除待ち状態を保存するSQLを準備する
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE todos SET delete_marked = 1 WHERE id = ?")) {
            // ★追加 更新対象のTodo番号をSQLの値として設定する
            statement.setInt(1, id);
            // ★追加 削除待ち状態の更新を実行する
            statement.executeUpdate();
        // ★追加 Todo削除待ち状態更新のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo削除待ち状態更新の失敗を知らせる
            throw new RuntimeException("Todoの取消線設定に失敗しました", e);
        // ★追加 Todo削除待ち状態更新処理を終了する
        }
    // ★追加 markTodoForDeletionメソッドを終了する
    }

    // ★追加 Todoの完了状態と削除待ち状態を取得する
    static int findTodoState(int id) {
        // ★追加 Todo状態を未完了として初期化する
        int state = 0;
        // ★追加 SELECT文と接続と結果を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             // ★追加 Todo状態を取得するSQLを準備する
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT done, delete_marked FROM todos WHERE id = ?")) {
            // ★追加 検索対象のTodo番号をSQLの値として設定する
            statement.setInt(1, id);
            // ★追加 SELECT結果を自動的に閉じる
            try (ResultSet rows = statement.executeQuery()) {
                // ★追加 Todoが存在する場合だけ状態を計算する
                if (rows.next()) {
                    // ★追加 完了済みを状態の上位ビットに設定する
                    state = rows.getInt("done") * 2;
                    // ★追加 削除待ち状態を状態の下位ビットに設定する
                    state += rows.getInt("delete_marked");
                }
            }
        // ★追加 Todo状態取得のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo状態取得の失敗を知らせる
            throw new RuntimeException("Todo状態の取得に失敗しました", e);
        // ★追加 Todo状態取得処理を終了する
        }
        // ★追加 取得したTodo状態を返す
        return state;
    // ★追加 findTodoStateメソッドを終了する
    }

    // ★追加 SELECTでSQLiteからTodo一覧を取得する
    static List<Todo> findAllTodos() {
        // ★追加 読み込んだTodoを入れる一覧を作る
        List<Todo> result = new ArrayList<>();
        // ★追加 SELECT文と接続と結果を自動的に閉じる
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, title, done, delete_marked FROM todos ORDER BY id");
             ResultSet rows = statement.executeQuery()) {
            // ★追加 SELECT結果を1行ずつTodoに変換する
            while (rows.next()) {
                // ★追加 SELECT結果からTodoを作成する
                Todo todo = new Todo(rows.getInt("id"), rows.getString("title"));
                // ★追加 SELECT結果の完了状態をTodoに設定する
                todo.setDone(rows.getInt("done") == 1);
                // ★追加 SELECT結果の削除待ち状態をTodoに設定する
                todo.setDeleteMarked(rows.getInt("delete_marked") == 1);
                // ★追加 読み込んだTodoを一覧に追加する
                result.add(todo);
            // ★追加 SELECT結果の読み込みを終了する
            }
        // ★追加 Todo一覧取得のエラーを扱う
        } catch (SQLException e) {
            // ★追加 Todo一覧取得の失敗を知らせる
            throw new RuntimeException("Todo一覧の取得に失敗しました", e);
        // ★追加 Todo一覧取得処理を終了する
        }
        // ★追加 SELECTしたTodo一覧を返す
        return result;
    // ★追加 findAllTodosメソッドを終了する
    }

    // ★追加 Todo一覧をJSON配列に変換する
    static String todosToJson() {
        // ★追加 JSON配列の本文を作成する
        StringBuilder json = new StringBuilder("[");
        // ★追加 SQLiteから全Todoを取得する
        List<Todo> todos = findAllTodos();
        // ★追加 Todo一覧を順番にJSONへ追加する
        for (int i = 0; i < todos.size(); i++) {
            // ★追加 2件目以降の前にカンマを追加する
            if (i > 0) {
                // ★追加 JSON要素の区切りを追加する
                json.append(",");
            }
            // ★追加 現在のTodoを取得する
            Todo todo = todos.get(i);
            // ★追加 タイトルと完了状態をJSONオブジェクトとして追加する
            json.append("{\"title\":\"")
                    // ★追加 タイトルをJSON用にエスケープして追加する
                    .append(escapeJson(todo.getTitle()))
                    // ★追加 完了状態の項目名を追加する
                    .append("\",\"done\":")
                    // ★追加 完了状態をJSONへ追加する
                    .append(todo.isDone())
                    // ★追加 JSONオブジェクトを閉じる
                    .append("}");
        // ★追加 Todo一覧のJSON変換を終了する
        }
        // ★追加 JSON配列の終了記号を追加する
        json.append("]");
        // ★追加 完成したJSON文字列を返す
        return json.toString();
    // ★追加 todosToJsonメソッドを終了する
    }

    // ★追加 JSON文字列内で特別な意味を持つ文字をエスケープする
    static String escapeJson(String value) {
        // ★追加 エスケープ後の文字列を作成する
        StringBuilder escaped = new StringBuilder();
        // ★追加 タイトルの文字を1文字ずつ確認する
        for (int i = 0; i < value.length(); i++) {
            // ★追加 現在の文字を取得する
            char character = value.charAt(i);
            // ★追加 文字の種類に応じてJSON用に変換する
            switch (character) {
                // ★追加 ダブルクォートをエスケープする
                case '"':
                    // ★追加 ダブルクォートの前にバックスラッシュを付ける
                    escaped.append("\\\"");
                    // ★追加 ダブルクォートの処理を終了する
                    break;
                // ★追加 バックスラッシュをエスケープする
                case '\\':
                    // ★追加 バックスラッシュを2文字にする
                    escaped.append("\\\\");
                    // ★追加 バックスラッシュの処理を終了する
                    break;
                // ★追加 改行をエスケープする
                case '\n':
                    // ★追加 改行をJSONのエスケープ表記にする
                    escaped.append("\\n");
                    // ★追加 改行の処理を終了する
                    break;
                // ★追加 復帰をエスケープする
                case '\r':
                    // ★追加 復帰をJSONのエスケープ表記にする
                    escaped.append("\\r");
                    // ★追加 復帰の処理を終了する
                    break;
                // ★追加 タブをエスケープする
                case '\t':
                    // ★追加 タブをJSONのエスケープ表記にする
                    escaped.append("\\t");
                    // ★追加 タブの処理を終了する
                    break;
                // ★追加 バックスペースをエスケープする
                case '\b':
                    // ★追加 バックスペースをJSONのエスケープ表記にする
                    escaped.append("\\b");
                    // ★追加 バックスペースの処理を終了する
                    break;
                // ★追加 改ページをエスケープする
                case '\f':
                    // ★追加 改ページをJSONのエスケープ表記にする
                    escaped.append("\\f");
                    // ★追加 改ページの処理を終了する
                    break;
                // ★追加 その他の制御文字をUnicode表記にする
                default:
                    // ★追加 制御文字かどうかを確認する
                    if (character < 0x20) {
                        // ★追加 制御文字を4桁のUnicodeエスケープに変換する
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        // ★追加 通常の文字をそのまま追加する
                        escaped.append(character);
                    }
                    // ★追加 通常文字または制御文字の処理を終了する
                    break;
            // ★追加 文字の種類分けを終了する
            }
        // ★追加 タイトル文字の確認を終了する
        }
        // ★追加 エスケープ後の文字列を返す
        return escaped.toString();
    // ★追加 escapeJsonメソッドを終了する
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
    // ★追加 削除待ち状態を保持する
    private boolean deleteMarked;

    // ★変更 Todo は done=false で初期化する
    // Todoの初期値を設定する
    Todo(int id, String title) {
        // Todoのidを設定する
        this.id = id;
        // Todoのタイトルを設定する
        this.title = title;
        // Todoを未完了状態で初期化する
        this.done = false;
        // ★追加 Todoを削除待ちでない状態に初期化する
        this.deleteMarked = false;
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
    // ★追加 削除待ち状態を返すメソッドを定義する
    boolean isDeleteMarked() {
        // ★追加 保持している削除待ち状態を返す
        return deleteMarked;
    }

    // ★追加 削除待ち状態を設定するメソッドを定義する
    void setDeleteMarked(boolean deleteMarked) {
        // ★追加 受け取った削除待ち状態を保存する
        this.deleteMarked = deleteMarked;
    }
    // Todoクラスの定義を終了する
}
