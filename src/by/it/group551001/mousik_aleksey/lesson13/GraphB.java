package by.it.group551001.mousik_aleksey.lesson13;

import java.util.*;

public class GraphB {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";

        Map<String, List<String>> adj = new HashMap<>();

        if (!line.isEmpty()) {
            for (String edge : line.split(",")) {
                String[] parts = edge.split("->");
                if (parts.length != 2) {
                    System.out.println("Неверный формат ребра: " + edge.trim());
                    return;
                }
                String from = parts[0].trim();
                String to = parts[1].trim();

                adj.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
                adj.computeIfAbsent(to, k -> new ArrayList<>());
            }
        }

        // Поиск цикла обходом в глубину: 0 - не посещена, 1 - в текущем пути, 2 - обработана
        Map<String, Integer> state = new HashMap<>();
        boolean hasCycle = false;

        for (String v : adj.keySet()) {
            if (!state.containsKey(v) && dfs(v, adj, state)) {
                hasCycle = true;
                break;
            }
        }

        System.out.println(hasCycle ? "yes" : "no");
    }

    private static boolean dfs(String v, Map<String, List<String>> adj, Map<String, Integer> state) {
        state.put(v, 1);
        for (String next : adj.get(v)) {
            Integer s = state.get(next);
            if (s == null) {
                if (dfs(next, adj, state)) {
                    return true;
                }
            } else if (s == 1) {
                return true; // вернулись в вершину текущего пути, значит есть цикл
            }
        }
        state.put(v, 2);
        return false;
    }
}