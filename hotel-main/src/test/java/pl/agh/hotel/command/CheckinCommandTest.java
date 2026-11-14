package pl.agh.hotel.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CheckinCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"),
            ZoneId.of("Europe/Warsaw"));

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;
    private PrintStream redirectedOut;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        redirectedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(redirectedOut);
        hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        hotel.addRoom(new Room(102, "Double room", 25000, 2));
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
        redirectedOut.close();
    }

    @Test
    void constructorRejectsNullDependencies() {
        try (Scanner scanner = new Scanner("")) {
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckinCommand(null, scanner, CLOCK));
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckinCommand(hotel, null, CLOCK));
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckinCommand(hotel, scanner, null));
        }
    }

    @Test
    void checksInMainAndAdditionalGuestWithProvidedDate() {
        executeWithInput(
                "102\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "1\n"
                        + "Anna\n"
                        + "Nowak\n"
                        + "2026-11-10\n"
                        + "3\n"
                        + " Late arrival \n");

        Stay stay = hotel.getStay(102);
        assertEquals("Jan", stay.getMainGuest().getFirstName());
        assertEquals("Kowalski", stay.getMainGuest().getLastName());
        assertEquals(1, stay.getAdditionalGuests().size());
        assertEquals("Anna", stay.getAdditionalGuests().getFirst().getFirstName());
        assertEquals("Nowak", stay.getAdditionalGuests().getFirst().getLastName());
        assertEquals(LocalDate.of(2026, 11, 10), stay.getCheckInDate());
        assertEquals(3, stay.getPlannedDays());
        assertEquals(LocalDate.of(2026, 11, 13), stay.getPlannedCheckOutDate());
        assertEquals("Late arrival", stay.getNotes());
        assertLastOutputLine("Check-in successful for room 102.");
    }

    @Test
    void blankDateUsesCurrentDateAndEmptyOptionalDataAreAccepted() {
        executeWithInput(
                "101\n"
                        + "Ewa\n"
                        + "Nowak\n"
                        + "0\n"
                        + "\n"
                        + "2\n"
                        + "\n");

        Stay stay = hotel.getStay(101);
        assertEquals(TODAY, stay.getCheckInDate());
        assertEquals(List.of(), stay.getAdditionalGuests());
        assertEquals(2, stay.getPlannedDays());
        assertEquals(LocalDate.of(2026, 11, 14), stay.getPlannedCheckOutDate());
        assertEquals("", stay.getNotes());
        assertLastOutputLine("Check-in successful for room 101.");
    }

    @Test
    void occupiedRoomIsRejectedWithoutChangingCurrentStay() {
        Stay currentStay = stay(TODAY.minusDays(1));
        hotel.checkIn(101, currentStay);

        executeWithInput("101\n");

        assertSame(currentStay, hotel.getStay(101));
        assertLastOutputLine("The room is occupied");
    }

    @Test
    void exceedingCapacityDoesNotOccupyRoom() {
        executeWithInput(
                "102\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "2\n");

        assertNull(hotel.getStay(102));
        assertLastOutputLine("Number of people cannot exceed the room capacity.");
    }

    @Test
    void negativeAdditionalGuestCountDoesNotOccupyRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "-1\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine("Number of additional guests cannot be negative.");
    }

    @Test
    void reportsNonNumericRoomNumber() {
        executeWithInput("not-a-number\n");

        assertNull(hotel.getStay(101));
        assertNull(hotel.getStay(102));
        assertLastOutputLine(
                "Room number, guest count and planned days must be integers.");
    }

    @Test
    void reportsNonNumericGuestCountWithoutOccupyingRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "many\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine(
                "Room number, guest count and planned days must be integers.");
    }

    @Test
    void reportsNonNumericPlannedDaysWithoutOccupyingRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "0\n"
                        + "2026-11-10\n"
                        + "many\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine(
                "Room number, guest count and planned days must be integers.");
    }

    @Test
    void reportsMissingRoom() {
        executeWithInput("999\n");

        assertLastOutputLine("A room with this number doesn't exist.");
    }

    @Test
    void invalidDateDoesNotOccupyRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "0\n"
                        + "not-a-date\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine("Invalid date. Use YYYY-MM-DD.");
    }

    @Test
    void futureDateDoesNotOccupyRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "0\n"
                        + "2026-11-13\n"
                        + "3\n"
                        + "\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine("Check-in date cannot be null or in the future.");
    }

    @Test
    void nonPositivePlannedDaysDoNotOccupyRoom() {
        executeWithInput(
                "101\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "0\n"
                        + "2026-11-10\n"
                        + "0\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine("Planned days must be positive.");
    }

    @Test
    void invalidGuestDataDoesNotOccupyRoom() {
        executeWithInput(
                "101\n"
                        + "   \n"
                        + "Kowalski\n");

        assertNull(hotel.getStay(101));
        assertLastOutputLine("First name can't be null or blank.");
    }

    private Stay stay(LocalDate checkInDate) {
        return new Stay(
                new Guest("Current", "Guest"),
                List.of(),
                checkInDate,
                2,
                "",
                CLOCK);
    }

    private void executeWithInput(String input) {
        try (Scanner scanner = new Scanner(input)) {
            CheckinCommand command = new CheckinCommand(hotel, scanner, CLOCK);
            command.execute();
        }
    }

    private void assertLastOutputLine(String expected) {
        List<String> outputLines = output.toString(StandardCharsets.UTF_8)
                .lines()
                .toList();
        assertEquals(expected, outputLines.getLast());
    }
}
