// HTTPサーバーを利用するためのクラスを読み込む
import com.sun.net.httpserver.HttpServer;
// HTTPリクエストを扱うためのクラスを読み込む
import com.sun.net.httpserver.HttpExchange;
// サーバーの待受アドレスを扱うクラスを読み込む
import java.net.InetSocketAddress;
// URLエンコードされた値を復号するクラスを読み込む
import java.net.URLDecoder;
// 文字コードを指定するクラスを読み込む
import java.nio.charset.StandardCharsets;
// データベース接続を扱うクラスを読み込む
import java.sql.Connection;
// SQLiteへ接続するクラスを読み込む
import java.sql.DriverManager;
// SQL例外を扱うクラスを読み込む
import java.sql.SQLException;
// パラメータ付きSQLを扱うクラスを読み込む
import java.sql.PreparedStatement;
// SQL検索結果を扱うクラスを読み込む
import java.sql.ResultSet;
// 可変長リストを扱うクラスを読み込む
import java.util.ArrayList;
// リストを扱うクラスを読み込む
import java.util.List;
// 並び替えを扱うクラスを読み込む
import java.util.Comparator;
// 順序を保持するマップを扱うクラスを読み込む
import java.util.LinkedHashMap;
// マップを扱うインターフェースを読み込む
import java.util.Map;

// Todoアプリケーションを定義する
public class App {
    // SQLiteデータベースの接続先を定義する
    static final String DB_URL = "jdbc:sqlite:todos.db";

    // アプリケーションを起動する
    public static void main(String[] args) throws Exception {
        // 起動時にテーブルを作成する
        initializeDatabase();
        // 8080番ポートでサーバーを作成する
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // ルートリクエストを処理する
        server.createContext("/", App::handleRequest);
        // 外部CSSファイルのリクエストを処理する
        server.createContext("/style.css", App::handleStyle);
        // サーバーを起動する
        server.start();
        // 起動先を表示する
        System.out.println("サーバー起動: http://localhost:8080");
    }

    // style.cssをレスポンスとして返す
    static void handleStyle(HttpExchange exchange) throws java.io.IOException {
        // CSSファイルをUTF-8のバイト列として読み込む
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("style.css"));
        // CSSのContent-Typeを設定する
        exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
        // 古いCSSをブラウザが再利用しないようにする
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        // CSSのレスポンスヘッダーを送る
        exchange.sendResponseHeaders(200, bytes.length);
        // CSS本文を書き込む
        exchange.getResponseBody().write(bytes);
        // CSSレスポンスを閉じる
        exchange.close();
    }

    // 参考画像の紙面風レイアウト用CSSを返す
    static String pageStyle() {
        // 淡い背景と中央の白い用紙を設定する
        return "*{box-sizing:border-box}body{margin:0;background:#e9edf2;color:#25282b;font-family:Arial,'Yu Gothic',sans-serif}.sheet{width:min(94vw,920px);min-height:calc(100vh - 48px);margin:24px auto;padding:54px 8%;background:#fffefa;border:1px solid #c7c8c5;box-shadow:0 8px 24px rgba(45,50,55,.08)}.page-header{text-align:center;margin-bottom:38px}.eyebrow{margin:0 0 12px;color:#a3a8ab;font-size:11px;letter-spacing:.28em}.page-header h1{margin:0;color:#202224;font-family:Georgia,'Yu Mincho',serif;font-size:clamp(42px,7vw,68px);font-weight:400;letter-spacing:.08em;text-transform:uppercase}.page-header h1 em{font-weight:400}.date-line{margin:15px auto 0;color:#9da6ad;font-family:Georgia,serif;font-size:15px;letter-spacing:.12em}.add-form,.filter-form{display:grid;grid-template-columns:2fr 1fr 1fr 1fr auto;gap:8px;margin:0 0 12px}.filter-form{grid-template-columns:2fr 1fr 1fr 1fr auto}.bulk-form{margin:20px 0 0;text-align:right}.add-form input,.filter-form input,.edit-form input,.filter-form select{min-width:0;padding:10px 11px;background:transparent;border:0;border-bottom:1px solid #bdc3c7;border-radius:0;color:#303438;font:inherit;outline:0}.add-form input:focus,.filter-form input:focus,.edit-form input:focus,.filter-form select:focus{border-bottom-color:#303438}.add-form button,.filter-form button,.bulk-form button,.edit-form button{padding:8px 14px;background:#303438;border:0;border-radius:2px;color:#fff;font:inherit;cursor:pointer}.bulk-form button{background:transparent;color:#6e7478;border:1px solid #b8bec2;font-size:12px}.summary{margin:18px 0 3px;color:#9ba1a5;text-align:right;font-size:12px;letter-spacing:.08em}.todo-list{margin:0;padding:0;list-style:none}.todo-row{display:grid;grid-template-columns:27px 1fr;gap:13px;align-items:center;min-height:68px;margin:0;padding:13px 0;border-bottom:1px dotted #555;background:transparent!important}.check-box{width:22px;height:22px;border:1px solid #aeb4b7;background:#fff}.todo-row.done .check-box{background:#303438;position:relative}.todo-row.done .check-box:after{position:absolute;left:5px;top:0;color:#fff;content:'✓';font-size:16px}.edit-form{display:grid;grid-template-columns:minmax(130px,2fr) 130px 120px 120px auto;gap:8px;align-items:center;margin:0}.edit-form input[name='title']{font-size:16px}.meta{grid-column:2;color:#989fa3;font-size:11px;letter-spacing:.04em}.actions{grid-column:2;display:flex;gap:13px}.actions a{color:#5c6469;font-size:12px;text-decoration:none}.actions a:hover{text-decoration:underline}.todo-row.done .edit-form input[name='title']{color:#9ca1a4;text-decoration:line-through}.page-footer{margin-top:42px;color:#b0b4b6;text-align:center;font-size:10px;letter-spacing:.28em}@media(max-width:700px){.sheet{width:calc(100vw - 24px);margin:12px;padding:36px 6%}.add-form,.filter-form{grid-template-columns:1fr 1fr}.add-form input[name='title'],.filter-form input[name='q']{grid-column:span 2}.add-form button,.filter-form button{grid-column:span 2}.edit-form{grid-template-columns:1fr 1fr}.edit-form input[name='title']{grid-column:span 2}.edit-form button{grid-column:span 2}.meta,.actions{grid-column:2}.todo-row{grid-template-columns:24px 1fr;gap:10px}}";
    }

    // HTTPリクエストを処理する
    static void handleRequest(HttpExchange exchange) throws java.io.IOException {
        // パスを取得する
        String path = exchange.getRequestURI().getPath();
        // CSSファイルのリクエストを確実に専用処理へ渡す
        if (path.equals("/style.css")) {
            // 外部CSSを返す
            handleStyle(exchange);
            // CSS処理を終了する
            return;
        }
        // HTTPメソッドを取得する
        String method = exchange.getRequestMethod();
        // POST本文またはGETクエリを解析する
        String input = method.equals("POST") ? new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8) : exchange.getRequestURI().getRawQuery();
        // 入力値をマップへ変換する
        Map<String, String> values = parseForm(input);
        // Todo追加を処理する
        if (path.equals("/add") && method.equals("POST")) {
            // タイトルを取り出す
            String title = values.getOrDefault("title", "").trim();
            // タイトルが空でない場合だけ追加する
            if (!title.isEmpty()) {
                // Todoを追加する
                insertTodo(title, values.getOrDefault("deadline", ""), values.getOrDefault("category", ""), values.getOrDefault("tags", ""));
            }
            // 一覧へ戻る
            redirect(exchange, "/");
            // 処理を終了する
            return;
        }
        // Todo編集を処理する
        if (path.equals("/edit") && method.equals("POST")) {
            // Todo番号を取得する
            Integer id = parseInteger(values.get("id"));
            // Todo番号が正しい場合だけ更新する
            if (id != null) {
                // Todoを更新する
                updateTodo(id, values.getOrDefault("title", "").trim(), values.getOrDefault("deadline", ""), values.getOrDefault("category", ""), values.getOrDefault("tags", ""));
            }
            // 一覧へ戻る
            redirect(exchange, "/");
            // 処理を終了する
            return;
        }
        // 完了状態の切替を処理する
        if (path.equals("/done") && method.equals("GET")) {
            // Todo番号を取得する
            Integer id = parseInteger(values.get("id"));
            // Todo番号が正しい場合だけ切り替える
            if (id != null) {
                // 完了状態を反転する
                toggleDone(id);
            }
            // 一覧へ戻る
            redirect(exchange, "/");
            // 処理を終了する
            return;
        }
        // 個別削除を処理する
        if (path.equals("/delete") && method.equals("GET")) {
            // Todo番号を取得する
            Integer id = parseInteger(values.get("id"));
            // Todo番号が正しい場合だけ削除する
            if (id != null) {
                // Todoを削除する
                executeUpdate("DELETE FROM todos WHERE id = ?", id);
            }
            // 一覧へ戻る
            redirect(exchange, "/");
            // 処理を終了する
            return;
        }
        // 完了済みの一括削除を処理する
        if (path.equals("/delete-completed") && method.equals("POST")) {
            // 完了済みTodoを削除する
            try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("DELETE FROM todos WHERE done = 1")) {
                // 削除SQLを実行する
                statement.executeUpdate();
            // 削除エラーを扱う
            } catch (SQLException e) {
                // 削除エラーを通知する
                throw new RuntimeException("完了済みTodoの削除に失敗しました", e);
            }
            // 一覧へ戻る
            redirect(exchange, "/");
            // 処理を終了する
            return;
        }
        // 一覧表示を処理する
        // 通常の一覧画面を表示する
        if (path.equals("/") && method.equals("GET")) {
            // 条件に合うTodoを取得する
            List<AppTodo> todos = findTodos(values.getOrDefault("status", "all"), values.getOrDefault("sort", "new"), values.getOrDefault("q", ""), values.getOrDefault("category", ""));
            // 一覧HTMLを作成する
            String html = renderPage(todos, values);
            // HTMLを返す
            send(exchange, html, "text/html; charset=UTF-8");
            // 処理を終了する
            return;
        }
        // 不明なパスを返す
        send(exchange, "ページが見つかりません", "text/plain; charset=UTF-8");
    }

    // データベースを初期化する
    static void initializeDatabase() {
        // Todoテーブルを作成する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 0, delete_marked INTEGER NOT NULL DEFAULT 0, deadline TEXT NOT NULL DEFAULT '', category TEXT NOT NULL DEFAULT '', tags TEXT NOT NULL DEFAULT '', created_at INTEGER NOT NULL DEFAULT 0)")) {
            // テーブル作成SQLを実行する
            statement.executeUpdate();
        // 初期化エラーを扱う
        } catch (SQLException e) {
            // 初期化失敗を通知する
            throw new RuntimeException("データベースの初期化に失敗しました", e);
        }
    }

    // Todoを追加する
    static void insertTodo(String title, String deadline, String category, String tags) {
        // Todo追加SQLを準備する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("INSERT INTO todos (title, done, delete_marked, deadline, category, tags, created_at) VALUES (?, 0, 0, ?, ?, ?, ?)")) {
            // タイトルを設定する
            statement.setString(1, title);
            // 締め切りを設定する
            statement.setString(2, deadline);
            // カテゴリを設定する
            statement.setString(3, category);
            // タグを設定する
            statement.setString(4, tags);
            // 作成日時を設定する
            statement.setLong(5, System.currentTimeMillis());
            // 追加SQLを実行する
            statement.executeUpdate();
        // 追加エラーを扱う
        } catch (SQLException e) {
            // 追加失敗を通知する
            throw new RuntimeException("Todoの追加に失敗しました", e);
        }
    }

    // Todoを更新する
    static void updateTodo(int id, String title, String deadline, String category, String tags) {
        // 空タイトルの場合は更新しない
        if (title.isEmpty()) {
            // 更新処理を終了する
            return;
        }
        // Todo更新SQLを準備する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("UPDATE todos SET title = ?, deadline = ?, category = ?, tags = ? WHERE id = ?")) {
            // タイトルを設定する
            statement.setString(1, title);
            // 締め切りを設定する
            statement.setString(2, deadline);
            // カテゴリを設定する
            statement.setString(3, category);
            // タグを設定する
            statement.setString(4, tags);
            // Todo番号を設定する
            statement.setInt(5, id);
            // 更新SQLを実行する
            statement.executeUpdate();
        // 更新エラーを扱う
        } catch (SQLException e) {
            // 更新失敗を通知する
            throw new RuntimeException("Todoの更新に失敗しました", e);
        }
    }

    // 完了状態を反転する
    static void toggleDone(int id) {
        // 完了状態反転SQLを準備する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("UPDATE todos SET done = CASE done WHEN 1 THEN 0 ELSE 1 END WHERE id = ?")) {
            // Todo番号を設定する
            statement.setInt(1, id);
            // 更新SQLを実行する
            statement.executeUpdate();
        // 更新エラーを扱う
        } catch (SQLException e) {
            // 更新失敗を通知する
            throw new RuntimeException("完了状態の更新に失敗しました", e);
        }
    }

    // ID付き更新SQLを実行する
    static void executeUpdate(String sql, int id) {
        // 更新SQLを準備する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement(sql)) {
            // Todo番号を設定する
            statement.setInt(1, id);
            // 更新SQLを実行する
            statement.executeUpdate();
        // 更新エラーを扱う
        } catch (SQLException e) {
            // 更新失敗を通知する
            throw new RuntimeException("Todoの処理に失敗しました", e);
        }
    }

    // 条件に合うTodoを取得する
    static List<AppTodo> findTodos(String status, String sort, String keyword, String category) {
        // 取得結果を作る
        List<AppTodo> result = new ArrayList<>();
        // Todo取得SQLを実行する
        try (Connection connection = DriverManager.getConnection(DB_URL); PreparedStatement statement = connection.prepareStatement("SELECT id, title, done, deadline, category, tags, created_at FROM todos ORDER BY created_at DESC, id DESC"); ResultSet rows = statement.executeQuery()) {
            // 検索結果を順番に処理する
            while (rows.next()) {
                // データベース行からTodoを作る
                AppTodo todo = new AppTodo(rows.getInt("id"), rows.getString("title"), rows.getInt("done") == 1, rows.getString("deadline"), rows.getString("category"), rows.getString("tags"), rows.getLong("created_at"));
                // 状態条件に合わないTodoを除外する
                if ((status.equals("active") && todo.done) || (status.equals("completed") && !todo.done)) {
                    // 次のTodoへ進む
                    continue;
                }
                // キーワード条件に合わないTodoを除外する
                if (!keyword.isBlank() && !todo.searchText().toLowerCase().contains(keyword.toLowerCase())) {
                    // 次のTodoへ進む
                    continue;
                }
                // カテゴリ条件に合わないTodoを除外する
                if (!category.isBlank() && !todo.category.equals(category)) {
                    // 次のTodoへ進む
                    continue;
                }
                // 条件に合うTodoを追加する
                result.add(todo);
            }
        // 取得エラーを扱う
        } catch (SQLException e) {
            // 取得失敗を通知する
            throw new RuntimeException("Todoの取得に失敗しました", e);
        }
        // 名前順が指定された場合に並べ替える
        if (sort.equals("name")) {
            // タイトルの昇順に並べ替える
            result.sort(Comparator.comparing(todo -> todo.title, String.CASE_INSENSITIVE_ORDER));
        }
        // 検索結果を返す
        return result;
    }

    // 一覧画面を作成する
    static String renderPage(List<AppTodo> todos, Map<String, String> values) {
        // 表示状態を取得する
        String status = values.getOrDefault("status", "all");
        // 並び順を取得する
        String sort = values.getOrDefault("sort", "new");
        // キーワードを取得する
        String keyword = values.getOrDefault("q", "");
        // カテゴリを取得する
        String category = values.getOrDefault("category", "");
        // 参考画像に合わせた紙面レイアウトのHTMLを開始する
        StringBuilder html = new StringBuilder("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1'><link rel='stylesheet' href='/style.css?v=3'><title>To-do List</title></head><body><main class='sheet'><header class='page-header'><p class='eyebrow'>✦ PLAN · DO · SHINE ✦</p><h1>To-do <em>List</em> ♡</h1><p class='date-line'>Plan your day, achieve your goals.</p></header>");
        // Todo追加フォームを表示する
        html.append("<form class='add-form' method='post' action='/add'><input name='title' placeholder='新しいタスクを入力' required><input type='date' name='deadline'><input name='category' placeholder='カテゴリ'><input name='tags' placeholder='タグ'><button>追加</button></form>");
        // 一覧操作を独立したセクションとして開始する
        html.append("<section class='list-controls'><div class='section-heading'><span class='summary'>").append(todos.size()).append("件</span></div>");
        // 絞り込みフォームを一覧操作エリアに表示する
        html.append("<form class='filter-form' method='get' action='/'><input name='category' value='").append(escapeHtml(category)).append("' placeholder='カテゴリ'><select name='status'><option value='all'").append(selected(status, "all")).append(">全部</option><option value='active'").append(selected(status, "active")).append(">未完了だけ</option><option value='completed'").append(selected(status, "completed")).append(">完了だけ</option></select><select name='sort'><option value='new'").append(selected(sort, "new")).append(">新しい順</option><option value='name'").append(selected(sort, "name")).append(">名前順</option></select><button type='button' onclick=\"document.getElementById('search-dialog').showModal()\">キーワード検索</button><button>表示を更新</button></form>");
        // キーワード検索用のポップアップ画面を表示する
        html.append("<dialog id='search-dialog' class='search-dialog'><form method='get' action='/'><h2>キーワード検索</h2><input name='q' value='").append(escapeHtml(keyword)).append("' placeholder='検索するキーワード'><input type='hidden' name='category' value='").append(escapeHtml(category)).append("'><input type='hidden' name='status' value='").append(escapeHtml(status)).append("'><input type='hidden' name='sort' value='").append(escapeHtml(sort)).append("'><div class='dialog-actions'><button type='button' onclick=\"document.getElementById('search-dialog').close()\">閉じる</button><button>検索する</button></div></form></dialog>");
        // 一括削除フォームを一覧操作エリアに表示する
        html.append("<form class='bulk-form' method='post' action='/delete-completed' onsubmit=\"return confirm('完了済みを削除しますか？')\"><button>完了済みを一括削除</button></form></section><ul class='todo-list'>");
        // Todoを一覧に追加する
        for (AppTodo todo : todos) {
            // 完了済みのクラスを設定する
            String cssClass = todo.done ? "todo done" : "todo";
            // Todo編集行を追加する
            html.append("<li class='todo-row ").append(cssClass).append("'><span class='check-box'></span><form class='edit-form' method='post' action='/edit'><input type='hidden' name='id' value='").append(todo.id).append("'><input name='title' value='").append(escapeHtml(todo.title)).append("' required><input type='date' name='deadline' value='").append(escapeHtml(todo.deadline)).append("'> <input name='category' value='").append(escapeHtml(todo.category)).append("' placeholder='カテゴリ'><input name='tags' value='").append(escapeHtml(todo.tags)).append("' placeholder='タグ'><button>保存</button></form><span class='meta'>締め切り: ").append(todo.deadline.isBlank() ? "未設定" : escapeHtml(todo.deadline)).append(" / ").append(todo.category.isBlank() ? "未分類" : escapeHtml(todo.category)).append(" / ").append(todo.tags.isBlank() ? "タグなし" : escapeHtml(todo.tags)).append("</span><span class='actions'><a href='/done?id=").append(todo.id).append("'>").append(todo.done ? "未完了に戻す" : "完了").append("</a> <a href='/delete?id=").append(todo.id).append("' onclick=\"return confirm('削除しますか？')\">削除</a></span></li>");
        }
        // HTMLを閉じる
        // 削除リンクから確認ポップアップを取り除く
        html.append("</ul><script>document.querySelectorAll('a[href^=\"/delete?\"]').forEach(function(link){link.removeAttribute(\"onclick\");});</script><footer class='page-footer'></footer></main></body></html>");
        // HTMLを返す
        return html.toString();
    }

    // 選択状態を作成する
    // 絞り込み専用画面のHTMLを作成する
    static String renderFilterPage(List<AppTodo> todos, Map<String, String> values) {
        // 現在の状態条件を取得する
        String status = values.getOrDefault("status", "all");
        // 現在の並び順を取得する
        String sort = values.getOrDefault("sort", "new");
        // 現在の検索語を取得する
        String keyword = values.getOrDefault("q", "");
        // 現在のカテゴリを取得する
        String category = values.getOrDefault("category", "");
        // 絞り込み画面のHTMLを開始する
        StringBuilder html = new StringBuilder("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1'><link rel='stylesheet' href='/style.css?v=3'><title>絞り込み</title></head><body><main class='sheet filter-page'><header class='page-header'><p class='eyebrow'>✦ FIND YOUR TASKS ✦</p><h1>Filter <em>List</em></h1><p class='date-line'>条件を選んでタスクを絞り込みます</p></header>");
        // 絞り込み条件のフォームを表示する
        html.append("<form class='filter-screen-form' method='get' action='/filter'><label>キーワード<input name='q' value='").append(escapeHtml(keyword)).append("' placeholder='タイトル・タグを検索'></label><label>カテゴリ<input name='category' value='").append(escapeHtml(category)).append("' placeholder='カテゴリ'></label><label>状態<select name='status'><option value='all'").append(selected(status, "all")).append(">全部</option><option value='active'").append(selected(status, "active")).append(">未完了だけ</option><option value='completed'").append(selected(status, "completed")).append(">完了だけ</option></select></label><label>並び順<select name='sort'><option value='new'").append(selected(sort, "new")).append(">新しい順</option><option value='name'").append(selected(sort, "name")).append(">名前順</option></select></label><button>絞り込む</button></form>");
        // 絞り込み結果の件数を表示する
        html.append("<p class='filter-result-count'>").append(todos.size()).append("件のタスク</p><ul class='todo-list'>");
        // 絞り込み結果を一覧表示する
        for (AppTodo todo : todos) {
            // 完了状態に応じたクラスを設定する
            String cssClass = todo.done ? "todo-row done" : "todo-row";
            // 絞り込み結果の行を作成する
            html.append("<li class='").append(cssClass).append("'><span class='check-box'></span><div class='filter-result-item'><strong>").append(escapeHtml(todo.title)).append("</strong><span class='meta'>締め切り: ").append(todo.deadline.isBlank() ? "未設定" : escapeHtml(todo.deadline)).append(" / ").append(todo.category.isBlank() ? "未分類" : escapeHtml(todo.category)).append(" / ").append(todo.tags.isBlank() ? "タグなし" : escapeHtml(todo.tags)).append("</span></div></li>");
        }
        // 通常一覧へ戻るリンクを表示する
        html.append("</ul><footer class='page-footer'><a href='/'>タスク一覧へ戻る</a></footer></main></body></html>");
        // 完成したHTMLを返す
        return html.toString();
    }

    // 選択状態のHTML属性を作成する
    static String selected(String actual, String expected) {
        // 値が一致したときだけ選択属性を返す
        return actual.equals(expected) ? " selected" : "";
    }

    // フォーム値を解析する
    static Map<String, String> parseForm(String input) {
        // 結果マップを作る
        Map<String, String> result = new LinkedHashMap<>();
        // 空入力を処理する
        if (input == null || input.isBlank()) {
            // 空マップを返す
            return result;
        }
        // パラメータを順番に処理する
        for (String part : input.split("&")) {
            // キーと値へ分割する
            String[] pair = part.split("=", 2);
            // 値が存在する場合だけ登録する
            if (pair.length == 2) {
                // URLデコードした値を登録する
                result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        // 解析結果を返す
        return result;
    }

    // 整数値を解析する
    static Integer parseInteger(String value) {
        // 数値変換を試みる
        try {
            // 数値またはnullを返す
            return value == null ? null : Integer.valueOf(value);
        // 不正な数値を処理する
        } catch (NumberFormatException e) {
            // 不正値をnullで返す
            return null;
        }
    }

    // HTML特殊文字を置換する
    static String escapeHtml(String value) {
        // nullを空文字へ変換する
        String safe = value == null ? "" : value;
        // 特殊文字をエスケープして返す
        return safe.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    // リダイレクトを返す
    static void redirect(HttpExchange exchange, String location) throws java.io.IOException {
        // Locationヘッダーを設定する
        exchange.getResponseHeaders().set("Location", location);
        // 303レスポンスを送る
        exchange.sendResponseHeaders(303, -1);
        // 接続を閉じる
        exchange.close();
    }

    // HTTPレスポンスを返す
    static void send(HttpExchange exchange, String body, String contentType) throws java.io.IOException {
        // 本文をUTF-8へ変換する
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        // Content-Typeを設定する
        exchange.getResponseHeaders().set("Content-Type", contentType);
        // レスポンスヘッダーを送る
        exchange.sendResponseHeaders(200, bytes.length);
        // 本文を書き込む
        exchange.getResponseBody().write(bytes);
        // 接続を閉じる
        exchange.close();
    }
}

// Todoデータを表すクラスを定義する
class AppTodo {
    // Todo番号を保持する
    final int id;
    // タイトルを保持する
    final String title;
    // 完了状態を保持する
    final boolean done;
    // 締め切りを保持する
    final String deadline;
    // カテゴリを保持する
    final String category;
    // タグを保持する
    final String tags;
    // Todoを作成する
    AppTodo(int id, String title, boolean done, String deadline, String category, String tags, long createdAt) {
        // Todo番号を設定する
        this.id = id;
        // タイトルを設定する
        this.title = title;
        // 完了状態を設定する
        this.done = done;
        // 締め切りを設定する
        this.deadline = deadline == null ? "" : deadline;
        // カテゴリを設定する
        this.category = category == null ? "" : category;
        // タグを設定する
        this.tags = tags == null ? "" : tags;
    }

    // 検索対象文字列を返す
    String searchText() {
        // タイトルと分類情報を連結する
        return title + " " + category + " " + tags;
    }
}
