package pl.agh.hotel.console;

import pl.agh.hotel.command.Command;
import pl.agh.hotel.utils.Map;
import pl.agh.hotel.utils.MyMap;

import java.util.Scanner;

public class ConsoleApplication {
    private final Scanner scanner;
    private final Map<String, Command> commands = new MyMap<>();
    private boolean running;

    public ConsoleApplication(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner can't be null");
        }
        this.scanner = scanner;
    }
    public void registerCommand(String name, Command command) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Command name can't be null or blank");
        }
        if (command == null) {
            throw new IllegalArgumentException(
                    "Command can't be null");
        }
        commands.put(name.toLowerCase(), command);
    }

    public void stop() {
        running = false;
    }
    public void run() {
        running = true;
        while (running && scanner.hasNextLine()) {
            String input = scanner.nextLine().trim().toLowerCase();
            Command command = commands.get(input);
            if (command == null) {
                System.out.println("Unknown command.");
            } else {
                command.execute();
            }
        }
    }


}