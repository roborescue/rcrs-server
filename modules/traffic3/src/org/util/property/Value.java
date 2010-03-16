package org.util.property;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @param <T> type of the value you want to handle
 */
public abstract class Value<T> {

    private String key;
    private T value;
    private List<ValueListener> listenerList;

    /**
     * Constructor.
     * @param k key
     * @param v value
     */
    public Value(String k, T v) {
        listenerList = new ArrayList<ValueListener>();
        setKey(k);
        setValue(v);
    }

    /**
     * set key of this value.
     * @param k key
     */
    public void setKey(String k) {
        key = k;
    }

    /**
     * get value.
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * set value.
     * @param v value
     */
    public void setValue(T v) {
        value = v;
        for (ValueListener listener : listenerList) {
            listener.valueChanged(this);
        }
    }

    /**
     * get value.
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * add value listener.
     * @param listener listener
     */
    public void addValueListener(ValueListener listener) {
        listenerList.add(listener);
    }

    /**
     * remove value listener.
     * @param listener listener
     */
    public void removeValueListener(ValueListener listener) {
        listenerList.remove(listener);
    }

    /**
     * get class of value.
     * @return class
     */
    public Class getValueClass() {
        return getValue().getClass();
    }
}
