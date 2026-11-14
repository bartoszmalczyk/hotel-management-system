package pl.agh.hotel.console;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConsoleApplicationTest {
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
    void constructorRejectsNullScanner() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConsoleApplication(null));
    }

    @Test
    void registerCommandRejectsInvalidArguments() {
        try (Scanner scanner = new Scanner("")) {
            ConsoleApplication application = new ConsoleApplication(scanner);

            assertThrows(IllegalArgumentException.class,
                    () -> application.registerCommand(null, () -> { }));
            assertThrows(IllegalArgumentException.class,
                    () -> application.registerCommand("", () -> { }));
            assertThrows(IllegalArgumentException.class,
                    () -> application.registerCommand("   ", () -> { }));
            assertThrows(IllegalArgumentException.class,
                    () -> application.registerCommand("test", null));
        }
    }

    @Test
    void executesCommandIgnoringCaseAndSurroundingWhitespace() {
        AtomicInteger executions = new AtomicInteger();
        try (Scanner scanner = new Scanner("  TeSt  \n")) {
            ConsoleApplication application = new ConsoleApplication(scanner);
            application.registerCommand(
                    " test ", () -> executions.incrementAndGet());

            application.run();
        }

        assertEquals(1, executions.get());
        assertEquals("", output());
    }

    @Test
    void registeringSameNormalizedNameReplacesCommand() {
        AtomicInteger firstExecutions = new AtomicInteger();
        AtomicInteger secondExecutions = new AtomicInteger();
        try (Scanner scanner = new Scanner("test\n")) {
            ConsoleApplication application = new ConsoleApplication(scanner);
            application.registerCommand(
                    "TEST", () -> firstExecutions.incrementAndGet());
            application.registerCommand(
                    " test ", () -> secondExecutions.incrementAndGet());

            application.run();
        }

        assertEquals(0, firstExecutions.get());
        assertEquals(1, secondExecutions.get());
    }

    @Test
    void unknownCommandIsReportedAndApplicationContinues() {
        AtomicInteger executions = new AtomicInteger();
        try (Scanner scanner = new Scanner("unknown\ntest\n")) {
            ConsoleApplication application = new ConsoleApplication(scanner);
            application.registerCommand(
                    "test", () -> executions.incrementAndGet());

            application.run();
        }

        assertEquals(1, executions.get());
        assertEquals(
                "Unknown command." + System.lineSeparator(),
                output());
    }

    @Test
    void stopPreventsExecutionOfFollowingCommands() {
        AtomicInteger laterExecutions = new AtomicInteger();
        try (Scanner scanner = new Scanner("stop\nlater\n")) {
            ConsoleApplication application = new ConsoleApplication(scanner);
            application.registerCommand("stop", () -> application.stop());
            application.registerCommand(
                    "later", () -> laterExecutions.incrementAndGet());

            application.run();
        }

        assertEquals(0, laterExecutions.get());
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
