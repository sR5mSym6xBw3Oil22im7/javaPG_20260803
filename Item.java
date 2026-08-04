// Item クラスの最小例です
public class Item {
    public static void main(String[] args) {
        // タイトルと完了状態を用意します
        String title = "自宅の庭のTodo";
        boolean done = false;

        // HTML の li 要素を作ります
        String html = "<li>" + title + "</li>";

        System.out.println(html);
        System.out.println(done);
    }
}
