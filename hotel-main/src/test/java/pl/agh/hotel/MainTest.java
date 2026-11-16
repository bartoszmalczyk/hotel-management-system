package pl.agh.hotel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.persistence.HotelCsvRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    private static final String HEADER = "recordType,roomNumber,description,priceInGrosz,"
            + "capacity,firstName,lastName,checkInDate,plannedDays,notes\n";
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;
    private PrintStream redirectedOut;

    @TempDir
    Path directory;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        redirectedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(redirectedOut);
        System.setIn(new ByteArrayInputStream("prices\nexit\n".getBytes(StandardCharsets.UTF_8)));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        redirectedOut.close();
    }

    @Test
    void loadsProvidedFileBeforeStartingCommands() throws IOException {
        Path file = Files.writeString(directory.resolve("hotel.csv"),
                HEADER + "ROOM,305,Imported room,12345,2,,,,,\n");

        Main.main(new String[] {file.toString()});

        assertEquals("Room: 305, price in grosze: 12345" + System.lineSeparator()
                + "Application closed." + System.lineSeparator(), output());
    }

    @Test
    void missingFileReportsErrorWithoutStartingCommands() {
        Path missing = directory.resolve("missing.csv");

        Main.main(new String[] {missing.toString()});

        assertEquals("CSV file not found: " + missing + System.lineSeparator(), output());
    }

    @Test
    void invalidFileDoesNotFallBackToDefaultOrPartiallyLoadedRooms() throws IOException {
        Path file = Files.writeString(directory.resolve("invalid.csv"), HEADER
                + "ROOM,305,Imported room,12345,2,,,,,\n"
                + "ROOM,305,Duplicate,10000,1,,,,,\n");

        Main.main(new String[] {file.toString()});

        assertTrue(output().startsWith("Could not load hotel: "));
        assertFalse(output().contains("Room: "));
        assertFalse(output().contains("Application closed."));
    }

    @Test
    void noArgumentsUseDefaultRooms() {
        Main.main(new String[0]);

        assertTrue(output().contains("Room: 101, price in grosze: 15000"));
        assertTrue(output().contains("Room: 102, price in grosze: 25000"));
        assertTrue(output().contains("Room: 201, price in grosze: 40000"));
        assertTrue(output().contains("Application closed."));
    }

    @Test
    void extraArgumentsDisplayUsageWithoutStartingCommands() {
        Main.main(new String[] {"one.csv", "two.csv"});

        assertTrue(output().startsWith("Usage: "));
        assertFalse(output().contains("Application closed."));
    }

    @Test
    void registeredSaveWritesFileAndLoopContinuesAfterError() throws IOException {
        Path file = directory.resolve("saved hotel.csv");
        String input = "save\n\nsave\n" + file + "\nexit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        Main.main(new String[0]);

        assertTrue(output().contains("Could not save hotel: Path can't be blank."));
        assertTrue(output().contains("Hotel saved successfully."));
        assertTrue(output().contains("Application closed."));
        Hotel restored = new HotelCsvRepository().load(file, Clock.systemDefaultZone());
        assertEquals(3, restored.getRooms().size());
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
