package pl.agh.hotel.utils;

import java.util.List;
import java.util.ArrayList;

public class MyMap<K, V> implements Map<K, V> {
    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();

    @Override
    public boolean put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key / value cannot be null");
        }
        if (!keys.contains(key)){
            keys.add(key);
            values.add(value);
        } else {
            values.set(keys.indexOf(key), value);
        }
        return true;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (keys.contains(key)) {
            return values.get(keys.indexOf(key));
        } else {
            return null;
        }
    }

    @Override
    public List<K> keys() {
        return new ArrayList<>(keys);
    }

    @Override
    public boolean remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (!keys.contains(key)) {
            return false;
        } else {
            values.remove(keys.indexOf(key));
            keys.remove(key);
            return true;
        }
    }

    @Override
    public boolean contains(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return keys.contains(key);
    }


}
