package pl.agh.hotel.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;
import pl.agh.hotel.persistence.HotelCsvRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveCommandTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"), ZoneId.of("Europe/Warsaw"));
    private final PrintStream originalOut = System.out;
    private final HotelCsvRepository repository = new HotelCsvRepository();
    private ByteArrayOutputStream output;
    private PrintStream redirectedOut;
    private Hotel hotel;
    private Stay stay;

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        redirectedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(redirectedOut);
        hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        hotel.addRoom(new Room(102, "Double room", 25000, 2));
        stay = new Stay(new Guest("Jan", "Kowalski"),
                List.of(new Guest("Anna", "Nowak")),
                LocalDate.of(2026, 11, 10), 3, "Late arrival", CLOCK);
        hotel.checkIn(102, stay);
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
                    () -> new SaveCommand(null, scanner, repository));
            assertThrows(IllegalArgumentException.class,
                    () -> new SaveCommand(hotel, null, repository));
            assertThrows(IllegalArgumentException.class,
                    () -> new SaveCommand(hotel, scanner, null));
        }
    }

    @Test
    void savesHotelToProvidedPathWithoutChangingStay() throws IOException {
        Path file = directory.resolve("saved hotel.csv");

        executeWithInput("  " + file + "  \n");

        assertEquals("Enter output CSV path:" + System.lineSeparator()
                + "Hotel saved successfully." + System.lineSeparator(), output());
        Hotel restored = repository.load(file, CLOCK);
        assertEquals(2, restored.getRooms().size());
        assertNull(restored.getStay(101));
        assertEquals("Jan", restored.getStay(102).getMainGuest().getFirstName());
        assertEquals("Anna", restored.getStay(102).getAdditionalGuests().getFirst().getFirstName());
        assertSame(stay, hotel.getStay(102));
    }

    @Test
    void reportsWriteFailureWithoutChangingStay() {
        executeWithInput(directory + "\n");

        assertTrue(output().contains("Could not save hotel: "));
        assertSame(stay, hotel.getStay(102));
    }

    @Test
    void reportsBlankPath() {
        executeWithInput("   \n");

        assertTrue(output().contains("Could not save hotel: Path can't be blank."));
        assertSame(stay, hotel.getStay(102));
    }

    @Test
    void reportsInvalidPathSyntax() {
        executeWithInput("bad\0path\n");

        assertTrue(output().contains("Could not save hotel: "));
        assertSame(stay, hotel.getStay(102));
    }

    @Test
    void endOfInputReportsMissingPath() {
        executeWithInput("");

        assertTrue(output().contains("Could not save hotel: no path provided."));
    }

    private void executeWithInput(String input) {
        try (Scanner scanner = new Scanner(input)) {
            new SaveCommand(hotel, scanner, repository).execute();
        }
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
