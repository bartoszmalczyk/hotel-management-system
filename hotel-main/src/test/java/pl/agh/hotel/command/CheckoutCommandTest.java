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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CheckoutCommandTest {
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
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
        redirectedOut.close();
    }

    @Test
    void constructorRejectsNullDependencies() {
        try (Scanner scanner = new Scanner("101\n")) {
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckoutCommand(null, scanner, CLOCK));
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckoutCommand(hotel, null, CLOCK));
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckoutCommand(hotel, scanner, null));
        }
    }

    @Test
    void checksOutOccupiedRoomAndDisplaysCharge() {
        hotel.checkIn(101, stay(TODAY.minusDays(3)));

        executeWithInput(" 101 \n", CLOCK);

        assertEquals(lines(
                "Enter room number:",
                "Check-out successful. Total in grosze: 45000"), output());
        assertNull(hotel.getStay(101));
    }

    @Test
    void reportsNonNumericRoomNumberWithoutChangingStay() {
        Stay stay = stay(TODAY.minusDays(1));
        hotel.checkIn(101, stay);

        executeWithInput("not-a-number\n", CLOCK);

        assertEquals(lines(
                "Enter room number:",
                "Room number must be an integer."), output());
        assertSame(stay, hotel.getStay(101));
    }

    @Test
    void reportsMissingRoom() {
        executeWithInput("999\n", CLOCK);

        assertEquals(lines(
                "Enter room number:",
                "A room with this number doesn't exist."), output());
    }

    @Test
    void reportsFreeRoom() {
        executeWithInput("101\n", CLOCK);

        assertEquals(lines(
                "Enter room number:",
                "Room is not occupied."), output());
        assertNull(hotel.getStay(101));
    }

    @Test
    void failedCheckOutDoesNotReleaseOccupiedRoom() {
        Stay stay = stay(TODAY);
        hotel.checkIn(101, stay);
        Clock earlierClock = Clock.fixed(
                LocalDate.of(2026, 11, 11)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant(),
                ZoneOffset.UTC);

        executeWithInput("101\n", earlierClock);

        assertEquals(lines(
                "Enter room number:",
                "Number of days cannot be negative."), output());
        assertSame(stay, hotel.getStay(101));
    }

    private Stay stay(LocalDate checkInDate) {
        return new Stay(
                new Guest("Jan", "Kowalski"),
                List.of(),
                checkInDate,
                3,
                "",
                CLOCK);
    }

    private void executeWithInput(String input, Clock clock) {
        try (Scanner scanner = new Scanner(input)) {
            CheckoutCommand command = new CheckoutCommand(hotel, scanner, clock);
            command.execute();
        }
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
