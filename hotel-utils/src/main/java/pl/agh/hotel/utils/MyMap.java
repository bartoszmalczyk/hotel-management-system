package pl.agh.hotel.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link Map} using two parallel lists.
 * A key and its value are stored at the same index in their respective lists.
 *
 * @param <K> type of keys
 * @param <V> type of values
 */
public class MyMap<K, V> implements Map<K, V> {
    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();

    @Override
    public boolean put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null.");
        }
        int index = keys.indexOf(key);
        if (index < 0) {
            keys.add(key);
            values.add(value);
        } else {
            values.set(index, value);
        }
        return true;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        int index = keys.indexOf(key);
        return index < 0 ? null : values.get(index);
    }

    @Override
    public List<K> keys() {
        return new ArrayList<>(keys);
    }

    @Override
    public boolean remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        int index = keys.indexOf(key);
        if (index < 0) {
            return false;
        }
        values.remove(index);
        keys.remove(index);
        return true;
    }

    @Override
    public boolean contains(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        return keys.contains(key);
    }
}
