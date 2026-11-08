package by.it.group551001.mousik_aleksey.lesson12;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MyAvlMap implements Map<Integer, String> {

    private static class Node {
        int key;
        String value;
        int height = 1;
        Node left;
        Node right;

        Node(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;
    private int size;

    // Вспомогательные поля, чтобы рекурсивные методы могли вернуть второй результат
    private String oldValue;
    private boolean removed;

    // ===================== Балансировка АВЛ =====================

    private static int height(Node n) {
        return n == null ? 0 : n.height;
    }

    private static void updateHeight(Node n) {
        int hl = height(n.left);
        int hr = height(n.right);
        n.height = (hl > hr ? hl : hr) + 1;
    }

    private static int balance(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    private static Node rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private static Node rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private static Node rebalance(Node n) {
        updateHeight(n);
        int b = balance(n);
        if (b > 1) {                         // левое поддерево слишком высокое
            if (balance(n.left) < 0) {       // случай «лево-право»
                n.left = rotateLeft(n.left);
            }
            return rotateRight(n);
        }
        if (b < -1) {                        // правое поддерево слишком высокое
            if (balance(n.right) > 0) {      // случай «право-лево»
                n.right = rotateRight(n.right);
            }
            return rotateLeft(n);
        }
        return n;
    }

    // ===================== Вставка и удаление =====================

    private Node put(Node n, int key, String value) {
        if (n == null) {
            size++;
            oldValue = null;
            return new Node(key, value);
        }
        if (key < n.key) {
            n.left = put(n.left, key, value);
        } else if (key > n.key) {
            n.right = put(n.right, key, value);
        } else {
            oldValue = n.value;  // ключ уже есть: заменяем значение, структура не меняется
            n.value = value;
            return n;
        }
        return rebalance(n);
    }

    private Node remove(Node n, int key) {
        if (n == null) {
            return null;
        }
        if (key < n.key) {
            n.left = remove(n.left, key);
        } else if (key > n.key) {
            n.right = remove(n.right, key);
        } else {
            if (n.left == null) {
                removed = true;
                oldValue = n.value;
                return n.right;
            }
            if (n.right == null) {
                removed = true;
                oldValue = n.value;
                return n.left;
            }
            // Два потомка: заменяем содержимое узла минимумом правого поддерева
            String val = n.value;
            Node min = n.right;
            while (min.left != null) {
                min = min.left;
            }
            n.key = min.key;
            n.value = min.value;
            n.right = remove(n.right, min.key); // удаляем минимум из правого поддерева
            oldValue = val;                     // возвращаем значение исходного узла
        }
        return rebalance(n);
    }

    private Node find(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        int k = (Integer) key;
        Node cur = root;
        while (cur != null) {
            if (k < cur.key) {
                cur = cur.left;
            } else if (k > cur.key) {
                cur = cur.right;
            } else {
                return cur;
            }
        }
        return null;
    }

    // ===================== Обязательные методы =====================

    @Override
    public String put(Integer key, String value) {
        if (key == null) {
            throw new NullPointerException("null-ключи не поддерживаются");
        }
        oldValue = null;
        root = put(root, key, value);
        String result = oldValue;
        oldValue = null;
        return result;
    }

    private Node removeNode(Node n, int key) {
        if (n == null) {
            return null;
        }
        if (key < n.key) {
            n.left = removeNode(n.left, key);
        } else if (key > n.key) {
            n.right = removeNode(n.right, key);
        } else {
            if (n.left == null) {
                removed = true;
                oldValue = n.value;
                return n.right;
            }
            if (n.right == null) {
                removed = true;
                oldValue = n.value;
                return n.left;
            }
            String val = n.value;
            Node min = n.right;
            while (min.left != null) {
                min = min.left;
            }
            n.key = min.key;
            n.value = min.value;
            n.right = removeNode(n.right, min.key);
            oldValue = val;
        }
        return rebalance(n);
    }

    @Override
    public String remove(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        removed = false;
        oldValue = null;
        root = removeNode(root, (Integer) key);
        String result = removed ? oldValue : null;
        if (removed) {
            size--;
        }
        removed = false;
        oldValue = null;
        return result;
    }

    @Override
    public String get(Object key) {
        Node n = find(key);
        return n == null ? null : n.value;
    }

    @Override
    public boolean containsKey(Object key) {
        return find(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    // Обход слева направо даёт ключи по возрастанию. Формат: {1=a, 2=b}
    @Override
    public String toString() {
        String[] acc = {"{"};
        appendInOrder(root, acc);
        return acc[0] + "}";
    }

    private void appendInOrder(Node n, String[] acc) {
        if (n == null) {
            return;
        }
        appendInOrder(n.left, acc);
        if (acc[0].length() > 1) {
            acc[0] += ", ";
        }
        acc[0] += n.key + "=" + n.value;
        appendInOrder(n.right, acc);
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<Integer, String>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
