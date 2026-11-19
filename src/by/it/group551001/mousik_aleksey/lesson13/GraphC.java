package by.it.group551001.mousik_aleksey.lesson13;

import java.util.*;

public class GraphC {

    private static Map<String, List<String>> adj = new TreeMap<>();
    private static Map<String, List<String>> radj = new TreeMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";

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
                radj.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
                radj.computeIfAbsent(from, k -> new ArrayList<>());
            }
        }

        // Алгоритм Косарайю, шаг 1: порядок завершения обхода по исходному графу
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();
        for (String v : adj.keySet()) {
            if (!visited.contains(v)) {
                dfs1(v, visited, order);
            }
        }

        // Шаг 2: обход транспонированного графа в обратном порядке завершения
        Map<String, Integer> compOf = new HashMap<>();
        List<List<String>> comps = new ArrayList<>();
        for (int i = order.size() - 1; i >= 0; i--) {
            String v = order.get(i);
            if (!compOf.containsKey(v)) {
                List<String> comp = new ArrayList<>();
                dfs2(v, compOf, comps.size(), comp);
                Collections.sort(comp);
                comps.add(comp);
            }
        }

        // Конденсация: рёбра между компонентами без дублей
        int n = comps.size();
        List<Set<Integer>> cadj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cadj.add(new HashSet<>());
        }
        int[] inDegree = new int[n];
        for (Map.Entry<String, List<String>> e : adj.entrySet()) {
            int a = compOf.get(e.getKey());
            for (String to : e.getValue()) {
                int b = compOf.get(to);
                if (a != b && cadj.get(a).add(b)) {
                    inDegree[b]++;
                }
            }
        }

        // Топологический порядок компонент (исток -> сток); при равнозначности - лексикографически
        PriorityQueue<Integer> queue = new PriorityQueue<>(
                Comparator.comparing((Integer i) -> comps.get(i).get(0)));
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            int c = queue.poll();
            for (String v : comps.get(c)) {
                sb.append(v);
            }
            sb.append("\n");
            for (int next : cadj.get(c)) {
                if (--inDegree[next] == 0) {
                    queue.add(next);
                }
            }
        }
        System.out.print(sb);
    }

    private static void dfs1(String v, Set<String> visited, List<String> order) {
        visited.add(v);
        for (String next : adj.get(v)) {
            if (!visited.contains(next)) {
                dfs1(next, visited, order);
            }
        }
        order.add(v);
    }

    private static void dfs2(String v, Map<String, Integer> compOf, int id, List<String> comp) {
        compOf.put(v, id);
        comp.add(v);
        for (String next : radj.get(v)) {
            if (!compOf.containsKey(next)) {
                dfs2(next, compOf, id, comp);
            }
        }
    }
}
