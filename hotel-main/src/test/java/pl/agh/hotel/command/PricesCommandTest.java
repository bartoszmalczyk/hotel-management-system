package pl.agh.hotel.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PricesCommandTest {
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
                () -> new PricesCommand(null));
    }

    @Test
    void displaysNothingWhenHotelHasNoRooms() {
        PricesCommand command = new PricesCommand(new Hotel());

        command.execute();

        assertEquals("", output());
    }

    @Test
    void displaysEveryRoomWithItsPrice() {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        hotel.addRoom(new Room(201, "Family room", 40000, 4));
        PricesCommand command = new PricesCommand(hotel);

        command.execute();

        List<String> outputLines = output().lines().toList();
        assertEquals(2, outputLines.size());

        // order is not guaranteed, so we check if output contains, not if it's equal
        assertTrue(outputLines.contains("Room: 101, price in grosze: 15000"));
        assertTrue(outputLines.contains("Room: 201, price in grosze: 40000"));
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
