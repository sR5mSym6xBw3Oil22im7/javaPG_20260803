// Todo 項目を出力するサンプルです
public class Item {
    // エントリーポイントです
    public static void main(String[] args) {
        // タイトルと完了状態を用意します
        String title = "自宅の庭のTodo";
        // 完了状態を用意します
        boolean done = false;

        // HTML の li 要素を作成します
        String html = "<li>" + title + "</li>";

        // HTML を表示します
        System.out.println(html);
        // 完了状態を表示します
        System.out.println(done);
    }
}
