import java.util.ArrayList; // Todo を入れるための箱として使うクラスを読み込む
import java.util.List; // 複数の Todo を並べて持つための型を読み込む

public class Main { // いちばん外側にある、実行の中心になるクラスです
    public static void main(String[] args) { // Java が最初に実行する入口です
        List<Todo> todos = new ArrayList<>(); // Todo をまとめて入れるリストを作る
        todos.add(new Todo("牛乳を買う", true)); // 1件目の Todo を追加する
        todos.add(new Todo("ゴミを出す", false)); // 2件目の Todo を追加する
        todos.add(new Todo("弁当を買う", false)); // 3件目の Todo を追加する

        for (Todo todo : todos) { // リストの中を1件ずつ順番に見る
            System.out.println(todo.toItem()); // toItem() の結果を1行ずつ表示する
        } // for 文の終わり
    } // main メソッドの終わり
} // Main クラスの終わり

class Todo { // Todo を表すクラスです
    private final String title; // タイトルを入れる場所です
    private final boolean done; // 済んだかどうかを入れる場所です

    public Todo(String title, boolean done) { // Todo を作るときの設定です
        this.title = title; // 渡されたタイトルを保存する
        this.done = done; // 渡された done を保存する
    } // コンストラクタの終わり

    public String toItem() { // Todo 1件を li タグの文字列に変える
        if (done) { // 済んでいるなら
            return "<li>[完了] " + title + "</li>"; // [済] を付けて返す
        } // if 文の終わり
        return "<li>" + title + "</li>"; // 済んでいないならそのまま返す
    } // toItem() の終わり
} // Todo クラスの終わり
