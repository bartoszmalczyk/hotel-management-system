package pl.agh.hotel.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoomTest {
    @Test
    void invalidRoomNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(-99, "A", 20, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Room(1005, "A", 20, 5));
    }

    @Test
    void blankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(505, "", 20, 5));
    }

    @Test
    void invalidCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(505, "A", 20, -999));
    }

    @Test
    void invalidPrice()   {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(505, "A", -999, 5));
    }
    @Test
    void gettersReturnProvidedValues() {
        Room room = new Room(505, "Double room", 25000, 2);

        assertEquals(505, room.getNumber());
        assertEquals("Double room", room.getDescription());
        assertEquals(25000, room.getPriceInGrosz());
        assertEquals(2, room.getCapacity());
    }

}
