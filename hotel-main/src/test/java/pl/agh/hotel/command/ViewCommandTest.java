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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ViewCommandTest {
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
        hotel.addRoom(new Room(102, "Double room", 25000, 3));
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
                    () -> new ViewCommand(null, scanner));
            assertThrows(IllegalArgumentException.class,
                    () -> new ViewCommand(hotel, null));
        }
    }

    @Test
    void displaysCompleteInformationForFreeRoom() {
        executeWithInput(" 101 \n");

        assertEquals(lines(
                "Enter room number:",
                "Room: 101",
                "Description: Single room",
                "Price in grosze: 15000",
                "Capacity: 1",
                "Room is free."), output());
    }

    @Test
    void displaysCompleteInformationForOccupiedRoom() {
        Guest mainGuest = new Guest("Jan", "Kowalski");
        Guest firstAdditionalGuest = new Guest("Anna", "Nowak");
        Guest secondAdditionalGuest = new Guest("Piotr", "Kowalski");
        Stay stay = new Stay(
                mainGuest,
                List.of(firstAdditionalGuest, secondAdditionalGuest),
                LocalDate.of(2026, 11, 10),
                3,
                "Late arrival",
                CLOCK);
        hotel.checkIn(102, stay);

        executeWithInput("102\n");

        assertEquals(lines(
                "Enter room number:",
                "Room: 102",
                "Description: Double room",
                "Price in grosze: 25000",
                "Capacity: 3",
                "Room is occupied.",
                "Main guest: Jan Kowalski",
                "Additional guests: 2",
                "- Anna Nowak",
                "- Piotr Kowalski",
                "Check-in date: 2026-11-10",
                "Planned days: 3",
                "Planned check-out date: 2026-11-13",
                "Notes: Late arrival"), output());
    }

    @Test
    void reportsNonNumericRoomNumber() {
        executeWithInput("not-a-number\n");

        assertEquals(lines(
                "Enter room number:",
                "Room number must be an integer."), output());
    }

    @Test
    void reportsMissingRoom() {
        executeWithInput("999\n");

        assertEquals(lines(
                "Enter room number:",
                "A room with this number doesn't exist."), output());
    }

    private void executeWithInput(String input) {
        try (Scanner scanner = new Scanner(input)) {
            ViewCommand command = new ViewCommand(hotel, scanner);
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
