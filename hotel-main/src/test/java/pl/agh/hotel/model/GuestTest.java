package pl.agh.hotel.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuestTest {
    @Test
    void invalidFirstName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest(null, "Kowalski"));
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("", "Kowalski"));
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("   ", "Kowalski"));
    }

    @Test
    void invalidLastName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("Jan", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("Jan", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("Jan", "   "));
    }

    @Test
    void gettersReturnProvidedValues() {
        Guest guest = new Guest("Jan", "Kowalski");

        assertEquals("Jan", guest.getFirstName());
        assertEquals("Kowalski", guest.getLastName());
    }
}
