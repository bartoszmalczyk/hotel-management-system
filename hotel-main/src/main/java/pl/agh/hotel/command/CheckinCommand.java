package pl.agh.hotel.command;

import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Stay;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckinCommand implements Command {
    private final Hotel hotel;
    private final Scanner scanner;
    private final Clock clock;

    public CheckinCommand(Hotel hotel, Scanner scanner, Clock clock) {
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
            System.out.println("Enter room number:");
            int number = Integer.parseInt(scanner.nextLine().trim());
            if (hotel.getStay(number) != null) {
                throw new IllegalStateException("The room is occupied");
            }
            System.out.println("Enter main guest's first name:");
            String firstName = scanner.nextLine().trim();
            System.out.println("Enter main guest's last name:");
            String lastName = scanner.nextLine().trim();
            Guest mainGuest = new Guest(firstName, lastName);

            System.out.println("Enter number of additional guests:");
            int additionalGuestCount = Integer.parseInt(scanner.nextLine().trim());
            if (additionalGuestCount < 0) {
                throw new IllegalArgumentException("Number of additional guests cannot be negative.");
            }
            if (additionalGuestCount > hotel.getRoom(number).getCapacity() - 1) {
                throw new IllegalArgumentException("Number of people cannot exceed the room capacity.");
            }

            List<Guest> additionalGuests = new ArrayList<>();
            for (int i = 0; i < additionalGuestCount; i++) {
                System.out.println("Enter additional guest " + (i + 1) + "'s first name:");
                String guestFirstName = scanner.nextLine().trim();
                System.out.println("Enter additional guest " + (i + 1) + "'s last name:");
                String guestLastName = scanner.nextLine().trim();
                additionalGuests.add(new Guest(guestFirstName, guestLastName));
            }
            System.out.println("Enter check-in date (YYYY-MM-DD), or leave blank for today:");
            String input = scanner.nextLine().trim();
            LocalDate checkInDate = input.isEmpty()
                    ? LocalDate.now(clock)
                    : LocalDate.parse(input);

            System.out.println("Enter planned number of days:");
            int plannedDays = Integer.parseInt(scanner.nextLine().trim());
            if (plannedDays <= 0) {
                throw new IllegalArgumentException("Planned days must be positive.");
            }

            System.out.println("Enter notes, or leave blank for none:");
            String notes = scanner.nextLine().trim();

            Stay stay = new Stay(mainGuest, additionalGuests, checkInDate, plannedDays, notes, clock);
            hotel.checkIn(number, stay);
            System.out.println("Check-in successful for room " + number + ".");

        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
        } catch (NumberFormatException exception) {
            System.out.println("Room number, guest count and planned days must be integers.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
