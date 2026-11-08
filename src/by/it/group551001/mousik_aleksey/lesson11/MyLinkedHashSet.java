package by.it.group551001.mousik_aleksey.lesson11;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class MyLinkedHashSet<E> implements Set<E> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private static class Node<E> {
        final E value;
        Node<E> next;    // следующий элемент в цепочке коллизий (односвязный список)
        Node<E> before;  // предыдущий элемент в порядке добавления
        Node<E> after;   // следующий элемент в порядке добавления

        Node(E value) {
            this.value = value;
        }
    }

    private Node<E>[] table;
    private Node<E> head; // самый ранний добавленный элемент
    private Node<E> tail; // самый поздний добавленный элемент
    private int size;

    @SuppressWarnings("unchecked")
    public MyLinkedHashSet() {
        table = (Node<E>[]) new Node[DEFAULT_CAPACITY];
    }

    private static int indexFor(Object o, int length) {
        int h = (o == null) ? 0 : o.hashCode();
        h ^= (h >>> 16);
        return h & (length - 1);
    }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private Node<E> find(Object o) {
        Node<E> cur = table[indexFor(o, table.length)];
        while (cur != null) {
            if (same(cur.value, o)) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] bigger = (Node<E>[]) new Node[table.length * 2];
        // Перераспределяем узлы, обходя их в порядке добавления.
        // Сам порядок (before/after) при этом не меняется.
        for (Node<E> cur = head; cur != null; cur = cur.after) {
            int idx = indexFor(cur.value, bigger.length);
            cur.next = bigger[idx];
            bigger[idx] = cur;
        }
        table = bigger;
    }

    // ===================== Основные методы =====================

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return find(o) != null;
    }

    @Override
    public boolean add(E e) {
        if (find(e) != null) {
            return false;
        }
        if (size + 1 > table.length * LOAD_FACTOR) {
            resize();
        }
        Node<E> node = new Node<>(e);

        // в цепочку коллизий
        int idx = indexFor(e, table.length);
        node.next = table[idx];
        table[idx] = node;

        // в конец списка порядка добавления
        node.before = tail;
        if (tail == null) {
            head = node;
        } else {
            tail.after = node;
        }
        tail = node;

        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int idx = indexFor(o, table.length);
        Node<E> prev = null;
        Node<E> cur = table[idx];
        while (cur != null) {
            if (same(cur.value, o)) {
                // убираем из цепочки коллизий
                if (prev == null) {
                    table[idx] = cur.next;
                } else {
                    prev.next = cur.next;
                }
                // убираем из списка порядка добавления
                if (cur.before == null) {
                    head = cur.after;
                } else {
                    cur.before.after = cur.after;
                }
                if (cur.after == null) {
                    tail = cur.before;
                } else {
                    cur.after.before = cur.before;
                }
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        table = (Node<E>[]) new Node[DEFAULT_CAPACITY];
        head = null;
        tail = null;
        size = 0;
    }

    // Элементы в порядке добавления, формат как у стандартных коллекций
    @Override
    public String toString() {
        String result = "[";
        for (Node<E> cur = head; cur != null; cur = cur.after) {
            if (cur != head) {
                result += ", ";
            }
            result += (cur.value == this) ? "(this Collection)" : String.valueOf(cur.value);
        }
        return result + "]";
    }

    // ===================== Групповые операции =====================

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c == this) {
            return false;
        }
        boolean changed = false;
        for (E e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == this) {
            boolean changed = size > 0;
            clear();
            return changed;
        }
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == this) {
            return false;
        }
        boolean changed = false;
        Node<E> cur = head;
        while (cur != null) {
            Node<E> nextInOrder = cur.after; // запоминаем до возможного удаления
            if (!c.contains(cur.value)) {
                remove(cur.value);
                changed = true;
            }
            cur = nextInOrder;
        }
        return changed;
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    // Итератор идёт в порядке добавления и поддерживает remove()
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> nextNode = head;
            private Node<E> lastReturned = null;

            @Override
            public boolean hasNext() {
                return nextNode != null;
            }

            @Override
            public E next() {
                if (nextNode == null) {
                    // NoSuchElementException лежит в java.util, поэтому не используем его
                    throw new IllegalStateException("No more elements");
                }
                lastReturned = nextNode;
                nextNode = nextNode.after;
                return lastReturned.value;
            }

            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                MyLinkedHashSet.this.remove(lastReturned.value);
                lastReturned = null;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> cur = head; cur != null; cur = cur.after) {
            result[i++] = cur.value;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
}