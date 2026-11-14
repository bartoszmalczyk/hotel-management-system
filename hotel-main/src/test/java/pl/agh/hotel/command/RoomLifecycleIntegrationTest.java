package pl.agh.hotel.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoomLifecycleIntegrationTest {
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
        hotel.addRoom(new Room(102, "Double room", 25000, 2));
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
        redirectedOut.close();
    }

    @Test
    void completesFullRoomLifecycleWithoutRetainingPreviousStay() {
        executeCheckin(
                "102\n"
                        + "Jan\n"
                        + "Kowalski\n"
                        + "1\n"
                        + "Anna\n"
                        + "Nowak\n"
                        + "2026-11-10\n"
                        + "3\n"
                        + "Late arrival\n");
        Stay firstStay = hotel.getStay(102);
        assertEquals("Jan", firstStay.getMainGuest().getFirstName());
        assertEquals(1, firstStay.getAdditionalGuests().size());
        output.reset();

        executeView("102\n");
        assertTrue(output().contains("Room is occupied."));
        assertTrue(output().contains("Main guest: Jan Kowalski"));
        assertTrue(output().contains("- Anna Nowak"));
        assertTrue(output().contains("Planned check-out date: 2026-11-13"));
        output.reset();

        new ListCommand(hotel).execute();
        assertEquals(
                "Room 102 is occupied, guests: "
                        + "Jan Kowalski Anna Nowak "
                        + "check-in date: 2026-11-10 "
                        + "planned check-out date: 2026-11-13"
                        + System.lineSeparator(),
                output());
        output.reset();

        executeCheckout("102\n");
        assertEquals(lines(
                "Enter room number:",
                "Check-out successful. Total in grosze: 50000"), output());
        assertNull(hotel.getStay(102));
        output.reset();

        executeCheckin(
                "102\n"
                        + "Ewa\n"
                        + "Zielinska\n"
                        + "0\n"
                        + "\n"
                        + "2\n"
                        + "New stay\n");

        Stay secondStay = hotel.getStay(102);
        assertNotSame(firstStay, secondStay);
        assertEquals("Ewa", secondStay.getMainGuest().getFirstName());
        assertEquals("Zielinska", secondStay.getMainGuest().getLastName());
        assertEquals(List.of(), secondStay.getAdditionalGuests());
        assertEquals(LocalDate.of(2026, 11, 12), secondStay.getCheckInDate());
        assertEquals("New stay", secondStay.getNotes());
    }

    private void executeCheckin(String input) {
        try (Scanner scanner = new Scanner(input)) {
            new CheckinCommand(hotel, scanner, CLOCK).execute();
        }
    }

    private void executeView(String input) {
        try (Scanner scanner = new Scanner(input)) {
            new ViewCommand(hotel, scanner).execute();
        }
    }

    private void executeCheckout(String input) {
        try (Scanner scanner = new Scanner(input)) {
            new CheckoutCommand(hotel, scanner, CLOCK).execute();
        }
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
