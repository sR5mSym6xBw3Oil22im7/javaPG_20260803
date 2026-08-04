// Todo の一覧を表示するサンプルです
public class Items {
    // エントリーポイントです
    public static void main(String[] args) {
        // Todo の一覧を用意します
        String[] todos = {
                "買い物をする",
                "",
                "犬を散歩する",
                "掃除をする"
        };

        // 各 Todo の完了状態です
        boolean[] done = { true, false, false, false };

        // 配列の先頭から順に処理します
        for (int i = 0; i < todos.length; i++) {
            // 空の項目はスキップします
            if (todos[i].isEmpty()) {
                continue;
            }

            // 完了済みなら印を付けます
            String status = done[i] ? "[済] " : "";
            // HTML の li 要素として表示します
            System.out.println("<li>" + status + todos[i] + "</li>");
        }
    }
}
