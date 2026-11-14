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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListCommandTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"),
            ZoneId.of("Europe/Warsaw"));

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;
    private PrintStream redirectedOut;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        redirectedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(redirectedOut);
    }

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
        redirectedOut.close();
    }

    @Test
    void constructorRejectsNullHotel() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListCommand(null));
    }

    @Test
    void displaysNothingWhenHotelHasNoRooms() {
        ListCommand command = new ListCommand(new Hotel());

        command.execute();

        assertEquals("", output());
    }

    @Test
    void displaysFreeRoom() {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        ListCommand command = new ListCommand(hotel);

        command.execute();

        assertEquals(
                "Room 101 is free." + System.lineSeparator(),
                output());
    }

    @Test
    void displaysOccupiedRoomWithGuestsAndDates() {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(201, "Family room", 40000, 4));
        Stay stay = new Stay(
                new Guest("Jan", "Kowalski"),
                List.of(
                        new Guest("Anna", "Nowak"),
                        new Guest("Piotr", "Kowalski")),
                LocalDate.of(2026, 11, 10),
                3,
                "",
                CLOCK);
        hotel.checkIn(201, stay);
        ListCommand command = new ListCommand(hotel);

        command.execute();

        assertEquals(
                "Room 201 is occupied, guests: "
                        + "Jan Kowalski Anna Nowak Piotr Kowalski "
                        + "check-in date: 2026-11-10 "
                        + "planned check-out date: 2026-11-13"
                        + System.lineSeparator(),
                output());
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
