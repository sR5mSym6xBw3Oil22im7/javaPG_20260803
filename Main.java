// ArrayList を使います
import java.util.ArrayList;
// List を使います
import java.util.List;

// Todo 一覧を表示するサンプルです
public class Main {
    // アプリの入口です
    public static void main(String[] args) {
        // Todo を入れるリストを作成します
        List<Todo> todos = new ArrayList<>();
        // 1件目の Todo を追加します
        todos.add(new Todo("買い物をする", true));
        // 2件目の Todo を追加します
        todos.add(new Todo("ゴミを出す", false));
        // 3件目の Todo を追加します
        todos.add(new Todo("掃除をする", false));

        // リストの中身を順に表示します
        for (Todo todo : todos) {
            // HTML の li 要素として出力します
            System.out.println(todo.toItem());
        }
    }
}

// Todo を表すクラスです
class Todo {
    // タイトルを保持します
    private final String title;
    // 完了状態を保持します
    private final boolean done;

    // Todo の内容を受け取って初期化します
    public Todo(String title, boolean done) {
        // 受け取ったタイトルを保存します
        this.title = title;
        // 受け取った状態を保存します
        this.done = done;
    }

    // Todo を li タグの文字列に変換します
    public String toItem() {
        // 完了している場合は印を付けます
        if (done) {
            return "<li>[済] " + title + "</li>";
        }
        // 未完了ならそのまま返します
        return "<li>" + title + "</li>";
    }
}
