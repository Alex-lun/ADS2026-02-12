package by.it.group551001.mousik_aleksey.lesson12;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

public class MyRbMap implements SortedMap<Integer, String> {

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    private static class Node {
        int key;
        String value;
        Node left;
        Node right;
        Node parent;
        boolean color = BLACK;

        Node(int key, String value, Node parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }
    }

    private Node root;
    private int size;

    // ===================== Вспомогательные методы (null-safe) =====================

    private static boolean colorOf(Node p) {
        return p == null ? BLACK : p.color;
    }

    private static Node parentOf(Node p) {
        return p == null ? null : p.parent;
    }

    private static void setColor(Node p, boolean c) {
        if (p != null) {
            p.color = c;
        }
    }

    private static Node leftOf(Node p) {
        return p == null ? null : p.left;
    }

    private static Node rightOf(Node p) {
        return p == null ? null : p.right;
    }

    private static Node successor(Node t) {
        if (t == null) {
            return null;
        }
        if (t.right != null) {
            Node p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }
        Node p = t.parent;
        Node ch = t;
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        return p;
    }

    private Node firstNode() {
        Node p = root;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
        }
        return p;
    }

    private Node lastNode() {
        Node p = root;
        if (p != null) {
            while (p.right != null) {
                p = p.right;
            }
        }
        return p;
    }

    private Node find(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        int k = (Integer) key;
        Node p = root;
        while (p != null) {
            if (k < p.key) {
                p = p.left;
            } else if (k > p.key) {
                p = p.right;
            } else {
                return p;
            }
        }
        return null;
    }

    // ===================== Повороты =====================

    private void rotateLeft(Node p) {
        if (p != null) {
            Node r = p.right;
            p.right = r.left;
            if (r.left != null) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == null) {
                root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;
        }
    }

    private void rotateRight(Node p) {
        if (p != null) {
            Node l = p.left;
            p.left = l.right;
            if (l.right != null) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == null) {
                root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;
        }
    }

    // ===================== Балансировка после вставки =====================

    private void fixAfterInsertion(Node x) {
        x.color = RED;
        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Node uncle = rightOf(parentOf(parentOf(x)));
                if (colorOf(uncle) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Node uncle = leftOf(parentOf(parentOf(x)));
                if (colorOf(uncle) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    // ===================== Удаление узла и балансировка =====================

    private void deleteNode(Node p) {
        size--;

        // Два потомка: копируем данные преемника и удаляем уже его
        if (p.left != null && p.right != null) {
            Node s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        }

        Node replacement = p.left != null ? p.left : p.right;

        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) {
                root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }
            p.left = null;
            p.right = null;
            p.parent = null;
            if (p.color == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == null) {
            root = null; // это был единственный узел
        } else {
            // у узла нет потомков: балансируем, пока он ещё в дереве, потом отцепляем
            if (p.color == BLACK) {
                fixAfterDeletion(p);
            }
            if (p.parent != null) {
                if (p == p.parent.left) {
                    p.parent.left = null;
                } else if (p == p.parent.right) {
                    p.parent.right = null;
                }
                p.parent = null;
            }
        }
    }

    private void fixAfterDeletion(Node x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Node sib = rightOf(parentOf(x));
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }
                if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else {
                Node sib = leftOf(parentOf(x));
                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }
                if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }
        setColor(x, BLACK);
    }

    // ===================== Обязательные методы =====================

    @Override
    public String put(Integer key, String value) {
        if (key == null) {
            throw new NullPointerException("null-ключи не поддерживаются");
        }
        int k = key;
        if (root == null) {
            root = new Node(k, value, null);
            size = 1;
            return null;
        }
        Node t = root;
        Node parent;
        do {
            parent = t;
            if (k < t.key) {
                t = t.left;
            } else if (k > t.key) {
                t = t.right;
            } else {
                String old = t.value; // ключ уже есть: заменяем значение
                t.value = value;
                return old;
            }
        } while (t != null);

        Node e = new Node(k, value, parent);
        if (k < parent.key) {
            parent.left = e;
        } else {
            parent.right = e;
        }
        fixAfterInsertion(e);
        size++;
        return null;
    }

    @Override
    public String remove(Object key) {
        Node p = find(key);
        if (p == null) {
            return null;
        }
        String old = p.value;
        deleteNode(p);
        return old;
    }

    @Override
    public String get(Object key) {
        Node p = find(key);
        return p == null ? null : p.value;
    }

    @Override
    public boolean containsKey(Object key) {
        return find(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Node p = firstNode(); p != null; p = successor(p)) {
            if (value == null ? p.value == null : value.equals(p.value)) {
                return true;
            }
        }
        return false;
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

    // Обход от минимального узла через successor: ключи по возрастанию. Формат: {1=a, 2=b}
    @Override
    public String toString() {
        String result = "{";
        for (Node p = firstNode(); p != null; p = successor(p)) {
            if (p != firstNode()) {
                result += ", ";
            }
            result += p.key + "=" + p.value;
        }
        return result + "}";
    }

    // ===================== Методы SortedMap =====================

    @Override
    public Integer firstKey() {
        Node p = firstNode();
        if (p == null) {
            throw new NoSuchElementException();
        }
        return p.key;
    }

    @Override
    public Integer lastKey() {
        Node p = lastNode();
        if (p == null) {
            throw new NoSuchElementException();
        }
        return p.key;
    }

    // Ключи строго меньше toKey (новая карта-копия, не представление)
    @Override
    public SortedMap<Integer, String> headMap(Integer toKey) {
        if (toKey == null) {
            throw new NullPointerException();
        }
        MyRbMap result = new MyRbMap();
        for (Node p = firstNode(); p != null && p.key < toKey; p = successor(p)) {
            result.put(p.key, p.value);
        }
        return result;
    }

    // Ключи больше или равны fromKey (новая карта-копия, не представление)
    @Override
    public SortedMap<Integer, String> tailMap(Integer fromKey) {
        if (fromKey == null) {
            throw new NullPointerException();
        }
        MyRbMap result = new MyRbMap();
        for (Node p = firstNode(); p != null; p = successor(p)) {
            if (p.key >= fromKey) {
                result.put(p.key, p.value);
            }
        }
        return result;
    }

    // Ключи от fromKey (включительно) до toKey (исключительно)
    @Override
    public SortedMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        if (fromKey == null || toKey == null) {
            throw new NullPointerException();
        }
        if (fromKey > toKey) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        MyRbMap result = new MyRbMap();
        for (Node p = firstNode(); p != null && p.key < toKey; p = successor(p)) {
            if (p.key >= fromKey) {
                result.put(p.key, p.value);
            }
        }
        return result;
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return null; // естественный порядок Integer
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    @Override
    public void putAll(Map<? extends Integer, ? extends String> m) {
        for (Map.Entry<? extends Integer, ? extends String> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
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
