package pl.agh.hotel.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.agh.hotel.console.ConsoleApplication;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExitCommandTest {
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
    void constructorRejectsNullApplication() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExitCommand(null));
    }

    @Test
    void stopsApplicationAndPreventsFollowingCommand() {
        AtomicInteger laterExecutions = new AtomicInteger();
        try (Scanner scanner = new Scanner("exit\nlater\n")) {
            ConsoleApplication application = new ConsoleApplication(scanner);
            application.registerCommand("exit", new ExitCommand(application));
            application.registerCommand(
                    "later", () -> laterExecutions.incrementAndGet());

            application.run();
        }

        assertEquals(0, laterExecutions.get());
        assertEquals(
                "Application closed." + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }
}
