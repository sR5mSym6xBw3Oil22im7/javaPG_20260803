import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class App {
    // ★変更 List と、次に振る番号を main の外に置く
    static List<Todo> todos = new ArrayList<>();
    // ★変更 次に振る番号は 1 から始める
    static int nextId = 1;

    public static void main(String[] args) throws Exception {
        // ★変更 起動時にサンプルの Todo を2件入れる
        todos.add(new Todo(nextId++, "牛乳を買う"));
        Todo egg = new Todo(nextId++, "卵を買う");
        egg.setDone(true);
        todos.add(egg);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String message;
            String method = exchange.getRequestMethod();
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

            if (path.equals("/add") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String value = body.substring(5);
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8);
                if (!title.isEmpty()) {
                    // ★変更 フォームの内容から Todo を1件作って List に追加する
                    todos.add(new Todo(nextId, title));
                    // ★変更 次の Todo に使う番号を進める
                    nextId++;
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if ((path.equals("/done") || path.equals("/delete")) && method.equals("GET")) {
                // ★追加 query から id を取り出す
                Integer id = parseId(exchange.getRequestURI().getQuery());
                // ★追加 id があるときだけ Todo を変える
                if (id != null) {
                    // ★追加 /done なら完了にする
                    if (path.equals("/done")) {
                        // ★追加 id が一致した Todo を探す
                        Todo todo = findTodoById(id);
                        // ★追加 見つかったときだけ done を true にする
                        if (todo != null) {
                            todo.setDone(true);
                        }
                    } else {
                        // ★追加 /delete なら List から外す
                        Todo todo = findTodoById(id);
                        // ★追加 見つかったときだけ List から消す
                        if (todo != null) {
                            todos.remove(todo);
                        }
                    }
                }
                // ★追加 どちらの操作でも "/" に戻す
                exchange.getResponseHeaders().set("Location", "/");
                // ★追加 303 で "/" に戻す
                exchange.sendResponseHeaders(303, -1);
                // ★追加 ここで処理を終える
                exchange.close();
                // ★追加 一覧表示の処理へ進まない
                return;
            } else if (path.equals("/")) {
                String html = "<form method='post' action='/add'>"
                        + "<input name='todo'><button>追加</button></form>"
                        + "<ul>";
                // ★変更 Todo の title を表示し、done のときだけ印を付ける
                // ★追加 各 Todo の横に完了と削除のリンクを付ける
                for (Todo todo : todos) {
                    String mark = "";
                    if (todo.isDone()) {
                        mark = " ✔";
                    }
                    // ★追加 id つきのリンクを Todo の横に表示する
                    html += "<li>" + todo.getTitle() + mark + " <a href='/done?id=" + todo.getId() + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                }
                html += "</ul>";
                message = html;
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else {
                // ★変更 未使用の /hello・/bye ルーティングを削除
                message = "ページが見つかりません";
            }
            byte[] responseBody = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.getResponseBody().close();
        });

        server.start();
        System.out.println("サーバー起動: http://localhost:8080 (止めるときは Ctrl+C)");
    }

    // ★追加 id から Todo を 1 件探す
    static Todo findTodoById(int id) {
        // ★追加 List を先頭から順に見る
        for (Todo todo : todos) {
            // ★追加 id が一致したらその Todo を返す
            if (todo.getId() == id) {
                return todo;
            }
        }
        // ★追加 見つからなければ null を返す
        return null;
    }

    // ★追加 query 文字列から id を読む
    static Integer parseId(String query) {
        // ★追加 query がないときは失敗にする
        if (query == null || query.isEmpty()) {
            return null;
        }
        // ★追加 & で分かれた部分を順に見る
        for (String part : query.split("&")) {
            // ★追加 id= で始まる部分だけ使う
            if (part.startsWith("id=")) {
                // ★追加 id= の後ろを数値に変える
                try {
                    return Integer.parseInt(part.substring(3));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        // ★追加 id がなければ失敗にする
        return null;
    }
}

// ★変更 Todo を表すクラスを追加
class Todo {
    private final int id;
    private final String title;
    private boolean done;

    // ★変更 Todo は done=false で初期化する
    Todo(int id, String title) {
        this.id = id;
        this.title = title;
        this.done = false;
    }

    // ★変更 id を読み出すメソッド
    int getId() {
        return id;
    }

    // ★変更 title を読み出すメソッド
    String getTitle() {
        return title;
    }

    // ★変更 done を読み出すメソッド
    boolean isDone() {
        return done;
    }

    // ★変更 done を書き換えるメソッド
    void setDone(boolean done) {
        this.done = done;
    }
}
