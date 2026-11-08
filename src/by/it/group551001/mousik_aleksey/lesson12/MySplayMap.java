package by.it.group551001.mousik_aleksey.lesson12;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

public class MySplayMap implements NavigableMap<Integer, String> {

    private static class Node {
        int key;
        String value;
        Node left;
        Node right;
        Node parent;

        Node(int key, String value, Node parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }
    }

    private Node root;
    private int size;

    // ===================== Splay =====================

    // Один поворот: x поднимается на место своего родителя
    private void rotate(Node x) {
        Node p = x.parent;
        Node g = p.parent;
        if (p.left == x) {
            p.left = x.right;
            if (x.right != null) {
                x.right.parent = p;
            }
            x.right = p;
        } else {
            p.right = x.left;
            if (x.left != null) {
                x.left.parent = p;
            }
            x.left = p;
        }
        p.parent = x;
        x.parent = g;
        if (g == null) {
            root = x;
        } else if (g.left == p) {
            g.left = x;
        } else {
            g.right = x;
        }
    }

    // Поднимает x в корень (zig, zig-zig, zig-zag)
    private void splay(Node x) {
        if (x == null) {
            return;
        }
        while (x.parent != null) {
            Node p = x.parent;
            Node g = p.parent;
            if (g == null) {
                rotate(x);                       // zig
            } else if ((g.left == p) == (p.left == x)) {
                rotate(p);                       // zig-zig
                rotate(x);
            } else {
                rotate(x);                       // zig-zag
                rotate(x);
            }
        }
    }

    // ===================== Поиск =====================

    // Ищет узел с ключом k. Найденный узел (а если ключа нет, то последний
    // посещённый) поднимается в корень.
    private Node find(int k) {
        Node cur = root;
        Node last = null;
        while (cur != null) {
            last = cur;
            if (k < cur.key) {
                cur = cur.left;
            } else if (k > cur.key) {
                cur = cur.right;
            } else {
                splay(cur);
                return cur;
            }
        }
        splay(last);
        return null;
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

    private static Node successor(Node t) {
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

    // mode: 0 = lower (<), 1 = floor (<=), 2 = ceiling (>=), 3 = higher (>)
    private Integer navigate(Integer key, int mode) {
        if (key == null) {
            throw new NullPointerException();
        }
        int k = key;
        Node cur = root;
        Node last = null;
        Node best = null;
        while (cur != null) {
            last = cur;
            if (mode == 0) {                       // lower
                if (k <= cur.key) {
                    cur = cur.left;
                } else {
                    best = cur;
                    cur = cur.right;
                }
            } else if (mode == 1) {                // floor
                if (k == cur.key) {
                    best = cur;
                    break;
                } else if (k < cur.key) {
                    cur = cur.left;
                } else {
                    best = cur;
                    cur = cur.right;
                }
            } else if (mode == 2) {                // ceiling
                if (k == cur.key) {
                    best = cur;
                    break;
                } else if (k > cur.key) {
                    cur = cur.right;
                } else {
                    best = cur;
                    cur = cur.left;
                }
            } else {                               // higher
                if (k >= cur.key) {
                    cur = cur.right;
                } else {
                    best = cur;
                    cur = cur.left;
                }
            }
        }
        splay(last); // сохраняем амортизированную оценку O(log n)
        return best == null ? null : best.key;
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
        Node cur = root;
        Node parent;
        do {
            parent = cur;
            if (k < cur.key) {
                cur = cur.left;
            } else if (k > cur.key) {
                cur = cur.right;
            } else {
                String old = cur.value;
                cur.value = value;
                splay(cur);
                return old;
            }
        } while (cur != null);

        Node e = new Node(k, value, parent);
        if (k < parent.key) {
            parent.left = e;
        } else {
            parent.right = e;
        }
        size++;
        splay(e);
        return null;
    }

    @Override
    public String remove(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        Node x = find((Integer) key); // x оказывается в корне
        if (x == null) {
            return null;
        }
        String old = x.value;
        Node l = x.left;
        Node r = x.right;
        x.left = null;
        x.right = null;
        if (l != null) {
            l.parent = null;
        }
        if (r != null) {
            r.parent = null;
        }
        if (l == null) {
            root = r;
        } else {
            root = l;
            Node m = l;
            while (m.right != null) {
                m = m.right;
            }
            splay(m);          // максимум левого поддерева становится корнем без правого потомка
            m.right = r;
            if (r != null) {
                r.parent = m;
            }
        }
        size--;
        return old;
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        Node n = find((Integer) key);
        return n == null ? null : n.value;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof Integer)) {
            return false;
        }
        return find((Integer) key) != null;
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

    // Ключи по возрастанию, формат {1=a, 2=b}
    @Override
    public String toString() {
        String result = "{";
        boolean first = true;
        for (Node p = firstNode(); p != null; p = successor(p)) {
            if (!first) {
                result += ", ";
            }
            result += p.key + "=" + p.value;
            first = false;
        }
        return result + "}";
    }

    // ===================== firstKey / lastKey =====================

    @Override
    public Integer firstKey() {
        Node p = firstNode();
        if (p == null) {
            throw new NoSuchElementException();
        }
        splay(p);
        return p.key;
    }

    @Override
    public Integer lastKey() {
        Node p = lastNode();
        if (p == null) {
            throw new NoSuchElementException();
        }
        splay(p);
        return p.key;
    }

    // ===================== lower / floor / ceiling / higher =====================

    @Override
    public Integer lowerKey(Integer key) {
        return navigate(key, 0);
    }

    @Override
    public Integer floorKey(Integer key) {
        return navigate(key, 1);
    }

    @Override
    public Integer ceilingKey(Integer key) {
        return navigate(key, 2);
    }

    @Override
    public Integer higherKey(Integer key) {
        return navigate(key, 3);
    }

    // ===================== headMap / tailMap / subMap (копии) =====================

    // Копирует узлы с lo <= key < hi (границы задаются флагами)
    private MySplayMap copyRange(Integer from, boolean fromInc, Integer to, boolean toInc) {
        MySplayMap result = new MySplayMap();
        for (Node p = firstNode(); p != null; p = successor(p)) {
            if (from != null && (fromInc ? p.key < from : p.key <= from)) {
                continue;
            }
            if (to != null && (toInc ? p.key > to : p.key >= to)) {
                break;
            }
            result.put(p.key, p.value);
        }
        return result;
    }

    // Ключи строго меньше toKey
    @Override
    public SortedMap<Integer, String> headMap(Integer toKey) {
        if (toKey == null) {
            throw new NullPointerException();
        }
        return copyRange(null, false, toKey, false);
    }

    // Ключи больше или равны fromKey
    @Override
    public SortedMap<Integer, String> tailMap(Integer fromKey) {
        if (fromKey == null) {
            throw new NullPointerException();
        }
        return copyRange(fromKey, true, null, false);
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey, boolean inclusive) {
        if (toKey == null) {
            throw new NullPointerException();
        }
        return copyRange(null, false, toKey, inclusive);
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey, boolean inclusive) {
        if (fromKey == null) {
            throw new NullPointerException();
        }
        return copyRange(fromKey, inclusive, null, false);
    }

    @Override
    public SortedMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, boolean fromInclusive,
                                                Integer toKey, boolean toInclusive) {
        if (fromKey == null || toKey == null) {
            throw new NullPointerException();
        }
        if (fromKey > toKey) {
            throw new IllegalArgumentException("fromKey > toKey");
        }
        return copyRange(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return null; // естественный порядок Integer
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends String> m) {
        for (Map.Entry<? extends Integer, ? extends String> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    @Override
    public Map.Entry<Integer, String> lowerEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> floorEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> ceilingEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> higherEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> firstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> lastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map.Entry<Integer, String> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<Integer, String> descendingMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<Integer> navigableKeySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<Integer> descendingKeySet() {
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
