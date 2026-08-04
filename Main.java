import java.util.ArrayList; // Todo を扱うための標準クラスです
import java.util.List; // 複数の Todo をまとめて扱うための型です

public class Main { // アプリの中心になるクラスです
    public static void main(String[] args) { // Java の実行開始点です
        List<Todo> todos = new ArrayList<>(); // Todo をまとめるリストを作ります
        todos.add(new Todo("買い物をする", true)); // 1件目の Todo
        todos.add(new Todo("ゴミを出す", false)); // 2件目の Todo
        todos.add(new Todo("掃除をする", false)); // 3件目の Todo

        for (Todo todo : todos) { // リストの中身を1件ずつ表示します
            System.out.println(todo.toItem()); // HTML の li 要素として出力します
        } // for の終わり
    } // main の終わり
} // Main クラスの終わり

class Todo { // Todo を表すクラスです
    private final String title; // タイトルを保持します
    private final boolean done; // 完了しているかを保持します

    public Todo(String title, boolean done) { // Todo を作るときの初期化です
        this.title = title; // 渡されたタイトルを保存します
        this.done = done; // 渡された状態を保存します
    } // コンストラクタの終わり

    public String toItem() { // Todo を li タグの文字列に変えます
        if (done) { // 完了しているなら印を付けます
            return "<li>[済] " + title + "</li>";
        } // if の終わり
        return "<li>" + title + "</li>"; // 未完了ならそのまま出します
    } // toItem() の終わり
} // Todo クラスの終わり
