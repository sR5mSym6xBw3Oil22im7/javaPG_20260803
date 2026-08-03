// Item クラス（Javaの部品のまとまり）を作ります
public class Item {
    // main メソッド（最初に動く処理）を書きます
    public static void main(String[] args) {
        // title という文字列（文字の集まり）を作って「自分の今日のTodo」を入れます
        String title = "自分の今日のTodo";
        // done という真偽値（true/false の値）を作って false を入れます
        boolean done = false;
        // + でつないで、1行分のHTML文字列（Webページの文章）を作ります
        String html = "<li>" + title + "</li>";
        // done の値をターミナル（黒い画面）に1行出します
        System.out.println(done);
    }
}
