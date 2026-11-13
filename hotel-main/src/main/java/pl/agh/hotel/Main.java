package pl.agh.hotel;

import pl.agh.hotel.command.*;
import pl.agh.hotel.console.ConsoleApplication;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;

import java.time.Clock;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Clock clock = Clock.systemDefaultZone();
        Hotel hotel = new Hotel();
        ConsoleApplication application = new ConsoleApplication(scanner);

        // hardcoded hotel rooms for now.
        hotel.addRoom(new Room(
                101, "Single room", 15000, 1));
        hotel.addRoom(new Room(
                102, "Double room", 25000, 2));
        hotel.addRoom(new Room(
                201, "Family room", 40000, 4));

        application.registerCommand("prices", new PricesCommand(hotel));
        application.registerCommand("view", new ViewCommand(hotel, scanner));
        application.registerCommand("checkin", new CheckinCommand(hotel, scanner, clock));
        application.registerCommand("checkout", new CheckoutCommand(hotel, scanner, clock));
        application.registerCommand("list", new ListCommand(hotel));
        application.registerCommand("exit", new ExitCommand(application));

        application.run();
    }
}