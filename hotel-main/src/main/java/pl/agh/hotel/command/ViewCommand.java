package pl.agh.hotel.command;

import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;

import java.util.Scanner;

/**
 * Reads a room number and displays complete information about the room and
 * its current stay. Invalid input is reported without terminating the command
 * loop.
 */
public class ViewCommand implements Command {
    private final Hotel hotel;
    private final Scanner scanner;

    /**
     * Creates a command using the supplied hotel and shared input scanner.
     *
     * @param hotel hotel to query; cannot be null
     * @param scanner shared scanner used to read the room number; cannot be
     *                null
     * @throws IllegalArgumentException when any dependency is null
     */
    public ViewCommand(Hotel hotel, Scanner scanner) {
        if (hotel == null) {
            throw new IllegalArgumentException(
                    "Hotel can't be null");
        }
        if (scanner == null) {
            throw new IllegalArgumentException(
                    "Scanner can't be null");
        }
        this.hotel = hotel;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("Enter room number:");
        try {
            int number = Integer.parseInt(scanner.nextLine().trim());
            Room room = hotel.getRoom(number);
            System.out.println("Room: " + room.getNumber());
            System.out.println("Description: " + room.getDescription());
            System.out.println("Price in grosze: " + room.getPriceInGrosz());
            System.out.println("Capacity: " + room.getCapacity());

            Stay stay = hotel.getStay(number);
            if (stay == null) {
                System.out.println("Room is free.");
            } else {
                System.out.println("Room is occupied.");
                Guest mainGuest = stay.getMainGuest();
                System.out.println(
                        "Main guest: " + mainGuest.getFirstName() + " "
                                + mainGuest.getLastName());
                System.out.println("Additional guests: " + stay.getAdditionalGuests().size());
                for (Guest guest : stay.getAdditionalGuests()) {
                    System.out.println("- " + guest.getFirstName() + " " + guest.getLastName());
                }
                System.out.println("Check-in date: " + stay.getCheckInDate());
                System.out.println("Planned days: " + stay.getPlannedDays());
                System.out.println(
                        "Planned check-out date: " + stay.getPlannedCheckOutDate());
                System.out.println("Notes: " + stay.getNotes());
            }
        } catch (NumberFormatException exception) {
            System.out.println("Room number must be an integer.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
