package rescuecore2.misc.gui;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import javax.swing.ListModel;
import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;

/**
   A java.util.List implementation that also implements javax.swing.ListModel. This class delegates to a real list for storing data.
   @param <T> The type of object that lives in this list.
 */
public class ListModelList<T> extends AbstractListModel implements List<T>, ListModel {
    private List<T> downstream;

    /**
       Construct a ListModelList backed by an ArrayList.
     */
    public ListModelList() {
        this(new ArrayList<T>());
    }

    /**
       Construct a ListModelList backed by a List.
       @param downstream The backing List.
     */
    public ListModelList(List<T> downstream) {
        this.downstream = downstream;
    }

    // ListModel interface
    @Override
    public Object getElementAt(int index) {
        return downstream.get(index);
    }

    @Override
    public int getSize() {
        return downstream.size();
    }

    // List interface
    @Override
    public boolean add(T t) {
        boolean result = downstream.add(t);
        if (result) {
            fireIntervalAdded(this, Math.max(0, downstream.size() - 1), Math.max(0, downstream.size() - 1));
        }
        return result;
    }

    @Override
    public void add(int index, T element) {
        downstream.add(index, element);
        fireContentsChanged(this, index, Math.max(0, downstream.size() - 1));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int oldSize = downstream.size();
        boolean result = downstream.addAll(c);
        fireIntervalAdded(this, oldSize, Math.max(0, downstream.size() - 1));
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = downstream.addAll(index, c);
        fireContentsChanged(this, index, Math.max(0, downstream.size() - 1));
        return result;
    }

    @Override
    public void clear() {
        int size = downstream.size();
        downstream.clear();
        fireIntervalRemoved(this, 0, Math.max(0, size - 1));
    }

    @Override
    public boolean contains(Object o) {
        return downstream.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return downstream.containsAll(c);
    }

    @Override
    public T get(int index) {
        return downstream.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return downstream.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return downstream.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new NotifyingIterator<T>(downstream.iterator());
    }

    @Override
    public int lastIndexOf(Object o) {
        return downstream.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new NotifyingListIterator<T>(downstream.listIterator());
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new NotifyingListIterator<T>(downstream.listIterator(index), index);
    }

    @Override
    public T remove(int index) {
        T result = downstream.remove(index);
        fireIntervalRemoved(this, index, index);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        boolean result = downstream.remove(o);
        if (result) {
            fireIntervalRemoved(this, index, index);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int oldSize = downstream.size();
        boolean result = downstream.removeAll(c);
        if (result) {
            fireIntervalRemoved(this, downstream.size(), Math.max(0, oldSize - 1));
            fireContentsChanged(this, 0, Math.max(0, downstream.size() - 1));
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = downstream.size();
        boolean result = downstream.retainAll(c);
        if (result) {
            fireIntervalRemoved(this, downstream.size(), Math.max(0, oldSize - 1));
            fireContentsChanged(this, 0, Math.max(0, downstream.size() - 1));
        }
        return result;
    }

    @Override
    public T set(int index, T element) {
        T result = downstream.set(index, element);
        fireContentsChanged(this, index, index);
        return result;
    }

    @Override
    public int size() {
        return downstream.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        ListModelList<T> sublist = new ListModelList<T>(downstream.subList(fromIndex, toIndex));
        for (ListDataListener l : getListDataListeners()) {
            sublist.addListDataListener(l);
        }
        return sublist;
    }

    @Override
    public Object[] toArray() {
        return downstream.toArray();
    }

    @Override
    public <S> S[] toArray(S[] a) {
        return downstream.toArray(a);
    }

    private class NotifyingIterator<T> implements Iterator<T> {
        private int index;
        private Iterator<T> downstream;

        NotifyingIterator(Iterator<T> downstream) {
            index = 0;
            this.downstream = downstream;
        }

        @Override
        public boolean hasNext() {
            return downstream.hasNext();
        }

        @Override
        public T next() {
            T result = downstream.next();
            ++index;
            return result;
        }

        @Override
        public void remove() {
            downstream.remove();
            fireIntervalRemoved(ListModelList.this, index, index);
        }
    }

    private class NotifyingListIterator<T> implements ListIterator<T> {
        private int index;
        private ListIterator<T> downstream;

        NotifyingListIterator(ListIterator<T> downstream) {
            this(downstream, 0);
        }

        NotifyingListIterator(ListIterator<T> downstream, int index) {
            this.index = index;
            this.downstream = downstream;
        }

        @Override
        public void add(T t) {
            downstream.add(t);
            ++index;
            fireIntervalAdded(ListModelList.this, index, index);
        }

        @Override
        public boolean hasNext() {
            return downstream.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return downstream.hasPrevious();
        }

        @Override
        public T next() {
            T result = downstream.next();
            ++index;
            return result;
        }

        @Override
        public int nextIndex() {
            return downstream.nextIndex();
        }

        @Override
        public T previous() {
            T result = downstream.previous();
            --index;
            return result;
        }

        @Override
        public int previousIndex() {
            return downstream.previousIndex();
        }

        @Override
        public void remove() {
            downstream.remove();
            fireIntervalRemoved(ListModelList.this, index, index);
        }

        @Override
        public void set(T t) {
            downstream.set(t);
            fireContentsChanged(ListModelList.this, index, index);
        }
    }
}
