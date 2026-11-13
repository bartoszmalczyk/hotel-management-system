package pl.agh.hotel.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StayTest {
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"), ZoneId.of("Europe/Warsaw"));
    private final LocalDate today = LocalDate.of(2026, 11, 12);
    private final Guest mainGuest = new Guest("Jan", "Kowalski");

    @Test
    void invalidMainGuest() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(null, List.of(), today, 3, "", clock));
    }

    @Test
    void invalidAdditionalGuests() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, null, today, 3, "", clock));

        List<Guest> guests = new ArrayList<>();
        guests.add(null);
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, guests, today, 3, "", clock));
    }

    @Test
    void invalidCheckInDate() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), null, 3, "", clock));
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), today.plusDays(1), 3, "", clock));
    }

    @Test
    void invalidClock() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), today, 3, "", null));
    }

    @Test
    void invalidPlannedDays() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), today, 0, "", clock));
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), today, -1, "", clock));
    }

    @Test
    void nullNotes() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stay(mainGuest, List.of(), today, 3, null, clock));
    }

    @Test
    void gettersReturnProvidedValues() {
        Guest additionalGuest = new Guest("Anna", "Kowalska");
        Stay stay = new Stay(mainGuest, List.of(additionalGuest), today, 3, "Late checkout", clock);

        assertEquals(mainGuest, stay.getMainGuest());
        assertEquals(List.of(additionalGuest), stay.getAdditionalGuests());
        assertEquals(today, stay.getCheckInDate());
        assertEquals(3, stay.getPlannedDays());
        assertEquals("Late checkout", stay.getNotes());
        assertEquals(LocalDate.of(2026, 11, 15), stay.getPlannedCheckOutDate());
    }

    @Test
    void pastCheckInDateAndEmptyOptionalDataAreAccepted() {
        Stay stay = new Stay(mainGuest, List.of(), today.minusDays(1), 1, "", clock);

        assertEquals(today.minusDays(1), stay.getCheckInDate());
        assertEquals(today, stay.getPlannedCheckOutDate());
        assertEquals(List.of(), stay.getAdditionalGuests());
        assertEquals("", stay.getNotes());
    }

    @Test
    void plannedCheckOutCrossesYearBoundary() {
        Stay stay = new Stay(mainGuest, List.of(), LocalDate.of(2025, 12, 31), 2, "", clock);

        assertEquals(LocalDate.of(2026, 1, 2), stay.getPlannedCheckOutDate());
    }

    @Test
    void additionalGuestsAreCopiedAndCannotBeModified() {
        Guest additionalGuest = new Guest("Anna", "Kowalska");
        List<Guest> guests = new ArrayList<>();
        guests.add(additionalGuest);
        Stay stay = new Stay(mainGuest, guests, today, 3, "", clock);

        guests.clear();

        assertEquals(List.of(additionalGuest), stay.getAdditionalGuests());
        assertThrows(UnsupportedOperationException.class,
                () -> stay.getAdditionalGuests().clear());
    }
}
