package by.it.group551001.mousik_aleksey.lesson11;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class MyTreeSet<E> implements Set<E> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] data = new Object[DEFAULT_CAPACITY]; // отсортированный массив
    private int size;
    private final Comparator<? super E> comparator;       // null = естественный порядок

    public MyTreeSet() {
        this.comparator = null;
    }

    public MyTreeSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    private int compare(Object a, Object b) {
        if (comparator != null) {
            return comparator.compare((E) a, (E) b);
        }
        return ((Comparable<Object>) a).compareTo(b);
    }

    // Бинарный поиск. Если элемент найден, возвращает его индекс,
    // иначе -(точка вставки) - 1.
    private int search(Object o) {
        int low = 0;
        int high = size - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = compare(data[mid], o);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }

    private void ensureCapacity() {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, size);
            data = bigger;
        }
    }

    private void removeAt(int index) {
        int moved = size - index - 1;
        if (moved > 0) {
            System.arraycopy(data, index + 1, data, index, moved);
        }
        data[--size] = null; // не удерживаем ссылку на удалённый элемент
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
        if (o == null) {
            throw new NullPointerException("null не поддерживается");
        }
        return search(o) >= 0;
    }

    @Override
    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException("null не поддерживается");
        }
        int pos = search(e);
        if (pos >= 0) {
            return false; // такой элемент уже есть
        }
        pos = -(pos + 1);
        ensureCapacity();
        System.arraycopy(data, pos, data, pos + 1, size - pos);
        data[pos] = e;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            throw new NullPointerException("null не поддерживается");
        }
        int pos = search(o);
        if (pos < 0) {
            return false;
        }
        removeAt(pos);
        return true;
    }

    @Override
    public void clear() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    // Элементы по возрастанию, формат как у стандартных коллекций
    @Override
    public String toString() {
        String result = "[";
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                result += ", ";
            }
            result += (data[i] == this) ? "(this Collection)" : String.valueOf(data[i]);
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

    // Один проход по массиву с уплотнением: O(n), без повторных сдвигов
    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == this) {
            return false;
        }
        int kept = 0;
        for (int i = 0; i < size; i++) {
            if (c.contains(data[i])) {
                data[kept++] = data[i];
            }
        }
        boolean changed = kept != size;
        for (int i = kept; i < size; i++) {
            data[i] = null;
        }
        size = kept;
        return changed;
    }

    // ============ Остальные методы интерфейса (не обязательны) ============

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int cursor = 0;
            private int lastReturned = -1;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                if (cursor >= size) {
                    throw new IllegalStateException("No more elements");
                }
                lastReturned = cursor++;
                return (E) data[lastReturned];
            }

            @Override
            public void remove() {
                if (lastReturned < 0) {
                    throw new IllegalStateException();
                }
                removeAt(lastReturned);
                cursor = lastReturned;
                lastReturned = -1;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(data, 0, result, 0, size);
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
}
