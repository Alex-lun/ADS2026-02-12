package by.it.group551001.mousik_aleksey.lesson13;
import java.util.*;

public class GraphA {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";

        // Граф: вершина -> список потомков; TreeMap/TreeSet дают лексикографический порядок
        Map<String, List<String>> adj = new TreeMap<>();
        Map<String, Integer> inDegree = new TreeMap<>();

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
                inDegree.putIfAbsent(from, 0);
                inDegree.merge(to, 1, Integer::sum);
            }
        }

        // Алгоритм Кана; PriorityQueue<String> всегда отдаёт лексикографически наименьшую вершину
        PriorityQueue<String> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String v = queue.poll();
            result.add(v);
            for (String next : adj.get(v)) {
                if (inDegree.merge(next, -1, Integer::sum) == 0) {
                    queue.add(next);
                }
            }
        }

        if (result.size() != adj.size()) {
            System.out.println("Граф содержит цикл, топологическая сортировка невозможна");
        } else {
            System.out.println(String.join(" ", result));
        }
    }
}
