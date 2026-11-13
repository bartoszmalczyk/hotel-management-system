package pl.agh.hotel;

import pl.agh.hotel.command.CheckinCommand;
import pl.agh.hotel.command.CheckoutCommand;
import pl.agh.hotel.command.ExitCommand;
import pl.agh.hotel.command.ListCommand;
import pl.agh.hotel.command.PricesCommand;
import pl.agh.hotel.command.ViewCommand;
import pl.agh.hotel.console.ConsoleApplication;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;

import java.time.Clock;
import java.util.Scanner;

/**
 * Starts the hotel management application, creates its initial room
 * configuration and registers all supported console commands.
 */
public class Main {
    /**
     * Creates the production application using standard input and the system
     * clock, then runs its command loop.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Clock clock = Clock.systemDefaultZone();
            Hotel hotel = new Hotel();
            ConsoleApplication application = new ConsoleApplication(scanner);

            // Hotel configuration is stored in code until CSV import is added.
            hotel.addRoom(new Room(
                    101, "Single room", 15000, 1));
            hotel.addRoom(new Room(
                    102, "Double room", 25000, 2));
            hotel.addRoom(new Room(
                    201, "Family room", 40000, 4));

            application.registerCommand("prices", new PricesCommand(hotel));
            application.registerCommand("view", new ViewCommand(hotel, scanner));
            application.registerCommand(
                    "checkin", new CheckinCommand(hotel, scanner, clock));
            application.registerCommand(
                    "checkout", new CheckoutCommand(hotel, scanner, clock));
            application.registerCommand("list", new ListCommand(hotel));
            application.registerCommand("exit", new ExitCommand(application));

            application.run();
        }
    }
}
