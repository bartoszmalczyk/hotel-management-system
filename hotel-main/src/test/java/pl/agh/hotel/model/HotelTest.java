package pl.agh.hotel.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HotelTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"),
            ZoneId.of("Europe/Warsaw"));

    private Hotel hotel;
    private Room room;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        room = new Room(101, "Single room", 15000, 1);
        hotel.addRoom(room);
    }

    @Test
    void roomCanBeAddedAndRetrieved() {
        Room secondRoom = new Room(102, "Double room", 25000, 2);

        hotel.addRoom(secondRoom);

        assertSame(room, hotel.getRoom(101));
        assertSame(secondRoom, hotel.getRoom(102));
        assertNull(hotel.getStay(102));
    }

    @Test
    void nullAndDuplicateRoomsAreRejectedWithoutReplacingExistingRoom() {
        Room duplicate = new Room(101, "Replacement", 30000, 2);

        assertThrows(IllegalArgumentException.class, () -> hotel.addRoom(null));
        assertThrows(IllegalArgumentException.class, () -> hotel.addRoom(duplicate));

        assertSame(room, hotel.getRoom(101));
        assertEquals(1, hotel.getRooms().size());
    }

    @Test
    void missingRoomCannotBeRetrieved() {
        assertThrows(IllegalArgumentException.class, () -> hotel.getRoom(999));
        assertThrows(IllegalArgumentException.class, () -> hotel.getStay(999));
    }

    @Test
    void roomsListIsIndependentFromHotel() {
        Room secondRoom = new Room(102, "Double room", 25000, 2);
        hotel.addRoom(secondRoom);

        List<Room> rooms = hotel.getRooms();
        rooms.clear();

        List<Room> roomsAfterModification = hotel.getRooms();
        assertEquals(2, roomsAfterModification.size());
        assertTrue(roomsAfterModification.contains(room));
        assertTrue(roomsAfterModification.contains(secondRoom));
    }

    @Test
    void checkInAssignsStayWhenGuestsFitExactly() {
        Room doubleRoom = new Room(102, "Double room", 25000, 2);
        hotel.addRoom(doubleRoom);
        Stay stay = stay(
                TODAY.minusDays(1),
                List.of(new Guest("Anna", "Nowak")));

        hotel.checkIn(102, stay);

        assertSame(stay, hotel.getStay(102));
    }

    @Test
    void nullStayAndMissingRoomAreRejected() {
        Stay stay = stay(TODAY, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkIn(101, null));
        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkIn(999, stay));

        assertNull(hotel.getStay(101));
    }

    @Test
    void occupiedRoomRejectsAnotherStayWithoutChangingCurrentStay() {
        Stay currentStay = stay(TODAY.minusDays(2), List.of());
        Stay anotherStay = stay(TODAY, List.of());
        hotel.checkIn(101, currentStay);

        assertThrows(IllegalStateException.class,
                () -> hotel.checkIn(101, anotherStay));

        assertSame(currentStay, hotel.getStay(101));
    }

    @Test
    void exceedingRoomCapacityDoesNotOccupyRoom() {
        Stay stay = stay(
                TODAY,
                List.of(new Guest("Anna", "Nowak")));

        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkIn(101, stay));

        assertNull(hotel.getStay(101));
    }

    @Test
    void checkOutCalculatesChargeForActualDaysAndReleasesRoom() {
        Stay stay = stay(LocalDate.of(2026, 11, 9), List.of());
        hotel.checkIn(101, stay);

        long charge = hotel.checkOut(101, CLOCK);

        assertEquals(45000, charge);
        assertNull(hotel.getStay(101));
    }

    @Test
    void sameDayCheckOutCostsZeroAndReleasesRoom() {
        hotel.checkIn(101, stay(TODAY, List.of()));

        long charge = hotel.checkOut(101, CLOCK);

        assertEquals(0, charge);
        assertNull(hotel.getStay(101));
    }

    @Test
    void checkOutCalculatesChargeAcrossMonthBoundary() {
        hotel.checkIn(101, stay(LocalDate.of(2026, 10, 30), List.of()));

        long charge = hotel.checkOut(101, CLOCK);

        assertEquals(13L * 15000, charge);
        assertNull(hotel.getStay(101));
    }

    @Test
    void freeAndMissingRoomsCannotBeCheckedOut() {
        assertThrows(IllegalStateException.class,
                () -> hotel.checkOut(101, CLOCK));
        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkOut(999, CLOCK));

        assertNull(hotel.getStay(101));
    }

    @Test
    void nullClockDoesNotReleaseOccupiedRoom() {
        Stay stay = stay(TODAY.minusDays(1), List.of());
        hotel.checkIn(101, stay);

        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkOut(101, null));

        assertSame(stay, hotel.getStay(101));
    }

    @Test
    void checkOutBeforeCheckInDateDoesNotReleaseRoom() {
        Stay stay = stay(TODAY, List.of());
        hotel.checkIn(101, stay);
        Clock earlierClock = clockAt(LocalDate.of(2026, 11, 11));

        assertThrows(IllegalArgumentException.class,
                () -> hotel.checkOut(101, earlierClock));

        assertSame(stay, hotel.getStay(101));
    }

    @Test
    void chargeOverflowDoesNotReleaseRoom() {
        Hotel expensiveHotel = new Hotel();
        Room expensiveRoom = new Room(999, "Expensive room", Integer.MAX_VALUE, 1);
        Stay veryLongStay = stay(LocalDate.MIN, List.of());
        expensiveHotel.addRoom(expensiveRoom);
        expensiveHotel.checkIn(999, veryLongStay);

        assertThrows(ArithmeticException.class,
                () -> expensiveHotel.checkOut(999, CLOCK));

        assertSame(veryLongStay, expensiveHotel.getStay(999));
    }

    @Test
    void roomCanBeOccupiedAgainAfterCheckOut() {
        Stay firstStay = stay(TODAY.minusDays(1), List.of());
        Stay secondStay = stay(TODAY, List.of());
        hotel.checkIn(101, firstStay);
        hotel.checkOut(101, CLOCK);

        hotel.checkIn(101, secondStay);

        assertSame(secondStay, hotel.getStay(101));
        assertNotSame(firstStay, hotel.getStay(101));
    }

    private Stay stay(LocalDate checkInDate, List<Guest> additionalGuests) {
        return new Stay(
                new Guest("Jan", "Kowalski"),
                additionalGuests,
                checkInDate,
                3,
                "",
                CLOCK);
    }

    private Clock clockAt(LocalDate date) {
        return Clock.fixed(
                date.atStartOfDay().toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC);
    }
}
