package pl.agh.hotel.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyMapTest {

    @Test
    void putStoresValueAndMakesKeyPresent() {
        MyMap<Integer, String> map = new MyMap<>();

        boolean result = map.put(101, "Room for one person");

        assertTrue(result);
        assertEquals("Room for one person", map.get(101));
        assertTrue(map.contains(101));
    }

    @Test
    void puttingSameKeyTwice() {
        MyMap<Integer, String> map = new MyMap<>();
        map.put(101, "A");
        boolean results = map.put(101, "B");

        assertTrue(results);
        assertEquals("B", map.get(101));
        assertEquals(List.of(101), map.keys());
    }

    @Test
    void equalKeysAreTreatedAsTheSameKey() {
        MyMap<String, String> map = new MyMap<>();
        String fristKey = new String("101");
        String secondKey = new String("101");

        assertNotSame(fristKey, secondKey);
        assertEquals(fristKey, secondKey);

        map.put(fristKey, "A");
        assertEquals("A", map.get(secondKey));
        assertTrue(map.contains(secondKey));

        boolean result = map.put(secondKey, "B");
        assertTrue(result);
        assertEquals("B", map.get(fristKey));
        assertEquals(List.of("101"), map.keys());
    }

    @Test
    void removingKeys() {
        MyMap<Integer, String> map = new MyMap<>();
        boolean resultNonExistingOne = map.remove(999);
        assertFalse(resultNonExistingOne);

        map.put(101, "A");
        boolean resultNonExistingTwo = map.remove(999);
        assertFalse(resultNonExistingTwo);

        assertEquals("A", map.get(101));
        assertEquals(List.of(101), map.keys());

        boolean resultExisting = map.remove(101);
        assertTrue(resultExisting);

        assertEquals(List.of(), map.keys());
    }

    @Test
    void removingFromTheMiddle() {
        MyMap<Integer, String> map = new MyMap<>();

        map.put(1, "A");
        map.put(2, "B");
        map.put(3, "C");

        boolean results = map.remove(2);
        assertTrue(results);
        assertNull(map.get(2));
        assertFalse(map.contains(2));
        assertEquals("A", map.get(1));
        assertEquals("C", map.get(3));
        assertEquals(List.of(1,3), map.keys());
    }

    @Test
    void modifyingReturnedKeysDoesNotChangeMap() {
        MyMap<Integer, String> map = new MyMap<>();
        map.put(101, "A");
        map.put(102, "B");

        List<Integer> returnedKeys = map.keys();
        returnedKeys.clear();
        returnedKeys.add(999);

        assertEquals(2, map.keys().size());
        assertTrue(map.contains(101));
        assertTrue(map.contains(102));
        assertFalse(map.contains(999));
        assertEquals("A", map.get(101));
        assertEquals("B", map.get(102));
    }

    @Test
    void invalidKeysOrValues() {
        MyMap<Integer, String> map = new MyMap<>();
        map.put(101, "A");

        try {
            map.put(null, "B");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        try {
            map.put(102, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        try {
            map.put(101, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        try {
            map.get(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        try {
            map.contains(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        try {
            map.remove(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            // Expected exception.
        }

        assertEquals(List.of(101), map.keys());
        assertEquals("A", map.get(101));
    }

}
