public class MyArrayDeque<E> {

    private static final int DEFAULT_CAPACITY = 16;
    private Object[] elements;
    private int head;
    private int tail;
    private int size;

    public MyArrayDeque() {
        this.elements = new Object[DEFAULT_CAPACITY];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(E element) {
        if (element == null) throw new NullPointerException();
        ensureCapacity();
        head = (head - 1 + elements.length) % elements.length;
        elements[head] = element;
        size++;
    }

    public void addLast(E element) {
        if (element == null) throw new NullPointerException();
        ensureCapacity();
        elements[tail] = element;
        tail = (tail + 1) % elements.length;
        size++;
    }

    public boolean add(E element) {
        addLast(element);
        return true;
    }

    @SuppressWarnings("unchecked")
    public E getFirst() {
        if (isEmpty()) throw new java.util.NoSuchElementException();
        return (E) elements[head];
    }

    @SuppressWarnings("unchecked")
    public E getLast() {
        if (isEmpty()) throw new java.util.NoSuchElementException();
        int lastIndex = (tail - 1 + elements.length) % elements.length;
        return (E) elements[lastIndex];
    }

    public E element() {
        return getFirst();
    }

    @SuppressWarnings("unchecked")
    public E pollFirst() {
        if (isEmpty()) return null;
        E result = (E) elements[head];
        elements[head] = null;
        head = (head + 1) % elements.length;
        size--;
        return result;
    }

    @SuppressWarnings("unchecked")
    public E pollLast() {
        if (isEmpty()) return null;
        tail = (tail - 1 + elements.length) % elements.length;
        E result = (E) elements[tail];
        elements[tail] = null;
        size--;
        return result;
    }

    public E poll() {
        return pollFirst();
    }

    @Override
    public String toString() {
        if (isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[(head + i) % elements.length]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            Object[] newElements = new Object[elements.length * 2];
            for (int i = 0; i < size; i++) {
                newElements[i] = elements[(head + i) % elements.length];
            }
            this.elements = newElements;
            this.head = 0;
            this.tail = size;
        }
    }
}