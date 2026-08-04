// Items クラスの例です
public class Items {
    public static void main(String[] args) {
        // Todo の一覧を用意します
        String[] todos = {
                "買い物をする",
                "",
                "犬を散歩する",
                "掃除をする"
        };

        // それぞれの Todo が完了しているかを表します
        boolean[] done = { true, false, false, false };

        // 配列の先頭から順に出力します
        for (int i = 0; i < todos.length; i++) {
            // 空の項目は飛ばします
            if (todos[i].isEmpty()) {
                continue;
            }

            // 完了済みなら印を付けます
            String status = done[i] ? "[済] " : "";
            System.out.println("<li>" + status + todos[i] + "</li>");
        }
    }
}
