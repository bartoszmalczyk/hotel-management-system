package pl.agh.hotel.console;

import pl.agh.hotel.command.Command;
import pl.agh.hotel.utils.Map;
import pl.agh.hotel.utils.MyMap;

import java.util.Locale;
import java.util.Scanner;

/**
 * Runs the console command loop and resolves user input through a registry of
 * {@link Command} implementations. Command names are handled without regard
 * to letter case, and unknown commands do not terminate the application.
 */
public class ConsoleApplication {
    private final Scanner scanner;
    private final Map<String, Command> commands = new MyMap<>();
    private boolean running;

    /**
     * Creates an application using the supplied input source. The scanner is
     * shared with interactive commands and is closed by its creator.
     *
     * @param scanner scanner used to read console input; cannot be null
     * @throws IllegalArgumentException when the scanner is null
     */
    public ConsoleApplication(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner can't be null");
        }
        this.scanner = scanner;
    }

    /**
     * Registers a command under a trimmed, lower-case name. Registering the
     * same normalized name again replaces the previous command.
     *
     * @param name command name; cannot be null or blank
     * @param command command implementation; cannot be null
     * @throws IllegalArgumentException when the name or command is invalid
     */
    public void registerCommand(String name, Command command) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Command name can't be null or blank");
        }
        if (command == null) {
            throw new IllegalArgumentException(
                    "Command can't be null");
        }
        commands.put(name.trim().toLowerCase(Locale.ROOT), command);
    }

    /**
     * Requests termination of the active command loop after the current
     * command finishes.
     */
    public void stop() {
        running = false;
    }

    /**
     * Reads and executes registered commands until {@link #stop()} is called
     * or the input source has no more lines. An unknown command produces a
     * message and leaves the loop running.
     */
    public void run() {
        running = true;
        while (running && scanner.hasNextLine()) {
            String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            Command command = commands.get(input);
            if (command == null) {
                System.out.println("Unknown command.");
            } else {
                command.execute();
            }
        }
    }
}
