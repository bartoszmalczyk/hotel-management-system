package pl.agh.hotel.utils;

import java.util.List;

/**
 * Defines a generic collection of key-value pairs.
 * Keys are compared using {@link Object#equals(Object)}. Null keys and values
 * are not supported.
 *
 * @param <K> type of keys
 * @param <V> type of values
 */
public interface Map<K, V> {
    /**
     * Adds a new key-value pair or replaces the value assigned to an existing
     * key.
     *
     * @param key key to add or update; cannot be null
     * @param value value to assign; cannot be null
     * @return {@code true} after adding or replacing the value
     * @throws IllegalArgumentException when the key or value is null
     */
    boolean put(K key, V value);

    /**
     * Returns the value assigned to the given key.
     *
     * @param key key to find; cannot be null
     * @return assigned value or {@code null} when the key is absent
     * @throws IllegalArgumentException when the key is null
     */
    V get(K key);

    /**
     * Returns an independent list containing all keys currently stored in the
     * map. Modifying the returned list does not modify this map.
     *
     * @return independent list of stored keys
     */
    List<K> keys();

    /**
     * Removes the given key and its assigned value.
     *
     * @param key key to remove; cannot be null
     * @return {@code true} when the pair was removed, or {@code false} when
     *         the key was absent
     * @throws IllegalArgumentException when the key is null
     */
    boolean remove(K key);

    /**
     * Checks whether the given key is present in the map.
     *
     * @param key key to find; cannot be null
     * @return {@code true} when the key is present
     * @throws IllegalArgumentException when the key is null
     */
    boolean contains(K key);
}
