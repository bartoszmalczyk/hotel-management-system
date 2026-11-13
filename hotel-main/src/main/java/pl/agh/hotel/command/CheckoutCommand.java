package pl.agh.hotel.command;

import pl.agh.hotel.model.Hotel;

import java.time.Clock;
import java.util.Scanner;

public class CheckoutCommand implements Command {
    private final Hotel hotel;
    private final Scanner scanner;
    private final Clock clock;

    public CheckoutCommand(Hotel hotel, Scanner scanner, Clock clock) {
        if (hotel == null) {
            throw new IllegalArgumentException("Hotel can't be null");
        }
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner can't be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock can't be null");
        }
        this.hotel = hotel;
        this.scanner = scanner;
        this.clock = clock;
    }

    @Override
    public void execute() {
        try{
            System.out.println("Enter room number");
            int number = Integer.parseInt(scanner.nextLine().trim());
            long totalPrice = hotel.checkOut(number, clock);
            System.out.println("Checkout has been successful. Total in grosze: " + totalPrice);
        } catch (NumberFormatException exception) {
            System.out.println("Room number must be an integer.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }


    }
}
