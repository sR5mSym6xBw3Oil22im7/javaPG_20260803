// Items クラスの開始
public class Items {
    // main メソッドの開始
    public static void main(String[] args) {
        // Todo の一覧を入れる配列
        String[] todos = {
                // 1件目のTodo
                "牛乳を買う",
                // 2件目は空欄
                "",
                // 3件目のTodo
                "卵を買う",
                // 4件目のTodo
                "掃除をする"
        };

        // 各 Todo が済んでいるかどうかを入れる配列
        boolean[] done = { true, false, false, false };

        // 配列の数だけ順番に見ていく
        for (int i = 1; i <= todos.length; i++) {
            // 空欄なら
            if (todos[i].isEmpty()) {
                // その行を飛ばす
                continue;
            }

            // 済みなら [済] を付ける
            String status = done[i] ? "[済] " : "";
            // 1件表示する
            System.out.println("<li>" + status + todos[i] + "</li>");
        }
    }
}
