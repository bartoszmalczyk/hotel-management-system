package pl.agh.hotel;

import pl.agh.hotel.command.CheckinCommand;
import pl.agh.hotel.command.CheckoutCommand;
import pl.agh.hotel.command.ExitCommand;
import pl.agh.hotel.command.ListCommand;
import pl.agh.hotel.command.PricesCommand;
import pl.agh.hotel.command.SaveCommand;
import pl.agh.hotel.command.ViewCommand;
import pl.agh.hotel.console.ConsoleApplication;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.persistence.HotelCsvRepository;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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
     * @param args optional single path to a hotel CSV file; without arguments,
     *             uses the built-in room configuration
     */
    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Usage: java -jar hotel-main-1.0-SNAPSHOT.jar [hotel.csv]");
            return;
        }

        Clock clock = Clock.systemDefaultZone();
        HotelCsvRepository repository = new HotelCsvRepository();
        Hotel hotel;
        if (args.length == 1) {
            try {
                hotel = repository.load(Path.of(args[0]), clock);
            } catch (NoSuchFileException exception) {
                System.out.println("CSV file not found: " + args[0]);
                return;
            } catch (IOException | IllegalArgumentException exception) {
                System.out.println("Could not load hotel: " + exception.getMessage());
                return;
            }
        } else {
            hotel = createDefaultHotel();
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleApplication application = new ConsoleApplication(scanner);

            application.registerCommand("prices", new PricesCommand(hotel));
            application.registerCommand("view", new ViewCommand(hotel, scanner));
            application.registerCommand(
                    "checkin", new CheckinCommand(hotel, scanner, clock));
            application.registerCommand(
                    "checkout", new CheckoutCommand(hotel, scanner, clock));
            application.registerCommand("list", new ListCommand(hotel));
            application.registerCommand("save", new SaveCommand(hotel, scanner, repository));
            application.registerCommand("exit", new ExitCommand(application));

            application.run();
        }
    }

    private static Hotel createDefaultHotel() {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        hotel.addRoom(new Room(102, "Double room", 25000, 2));
        hotel.addRoom(new Room(201, "Family room", 40000, 4));
        return hotel;
    }
}
