package by.it.group551001.mousik_aleksey.lesson11;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class MyHashSet<E> implements Set<E> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    // Узел односвязного списка (цепочка для элементов с коллизиями)
    private static class Node<E> {
        final E value;
        Node<E> next;

        Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<E>[] table;
    private int size;

    @SuppressWarnings("unchecked")
    public MyHashSet() {
        table = (Node<E>[]) new Node[DEFAULT_CAPACITY];
    }

    // Индекс корзины; длина таблицы всегда степень двойки
    private static int indexFor(Object o, int length) {
        int h = (o == null) ? 0 : o.hashCode();
        h ^= (h >>> 16); // перемешиваем биты хеш-кода
        return h & (length - 1);
    }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] old = table;
        Node<E>[] bigger = (Node<E>[]) new Node[old.length * 2];
        for (Node<E> head : old) {
            Node<E> cur = head;
            while (cur != null) {
                Node<E> next = cur.next;
                int idx = indexFor(cur.value, bigger.length);
                cur.next = bigger[idx];
                bigger[idx] = cur;
                cur = next;
            }
        }
        table = bigger;
    }

    // ===================== Обязательные методы =====================

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
        Node<E> cur = table[indexFor(o, table.length)];
        while (cur != null) {
            if (same(cur.value, o)) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    @Override
    public boolean add(E e) {
        if (contains(e)) {
            return false;
        }
        if (size + 1 > table.length * LOAD_FACTOR) {
            resize();
        }
        int idx = indexFor(e, table.length);
        table[idx] = new Node<>(e, table[idx]); // вставка в начало цепочки
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
                if (prev == null) {
                    table[idx] = cur.next;
                } else {
                    prev.next = cur.next;
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
        size = 0;
    }

    @Override
    public String toString() {
        String result = "[";
        boolean first = true;
        for (Node<E> head : table) {
            for (Node<E> cur = head; cur != null; cur = cur.next) {
                if (!first) {
                    result += ", ";
                }
                result += (cur.value == this) ? "(this Collection)" : String.valueOf(cur.value);
                first = false;
            }
        }
        return result + "]";
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
