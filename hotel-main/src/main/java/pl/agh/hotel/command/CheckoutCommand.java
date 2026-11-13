package pl.agh.hotel.command;

import pl.agh.hotel.model.Hotel;

import java.time.Clock;
import java.util.Scanner;

/**
 * Reads an occupied room number, checks the guest out using the current date,
 * and displays the resulting charge in grosze. Invalid input does not release
 * the room.
 */
public class CheckoutCommand implements Command {
    private final Hotel hotel;
    private final Scanner scanner;
    private final Clock clock;

    /**
     * Creates a command using a hotel, shared input scanner and replaceable
     * source of the check-out date.
     *
     * @param hotel hotel in which the room will be released; cannot be null
     * @param scanner shared scanner used to read the room number; cannot be
     *                null
     * @param clock source of the check-out date; cannot be null
     * @throws IllegalArgumentException when any dependency is null
     */
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
        try {
            System.out.println("Enter room number:");
            int number = Integer.parseInt(scanner.nextLine().trim());
            long totalPrice = hotel.checkOut(number, clock);
            System.out.println(
                    "Check-out successful. Total in grosze: " + totalPrice);
        } catch (NumberFormatException exception) {
            System.out.println("Room number must be an integer.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
