package pl.agh.hotel.command;

import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;

import java.util.List;

/**
 * Displays all rooms together with their occupancy, current guests and stay
 * dates.
 */
public class ListCommand implements Command {
    private final Hotel hotel;

    /**
     * Creates a command operating on the supplied hotel.
     *
     * @param hotel hotel whose rooms will be listed; cannot be null
     * @throws IllegalArgumentException when the hotel is null
     */
    public ListCommand(Hotel hotel) {
        if (hotel == null) {
            throw new IllegalArgumentException(
                    "Hotel can't be null.");
        }
        this.hotel = hotel;
    }

    @Override
    public void execute() {
        List<Room> allRooms = hotel.getRooms();
        for (Room room : allRooms) {
            Stay stay = hotel.getStay(room.getNumber());
            if (stay != null) {
                System.out.print("Room " + room.getNumber() + " is occupied, guests: ");
                System.out.print(
                        stay.getMainGuest().getFirstName() + " "
                                + stay.getMainGuest().getLastName() + " ");
                for (Guest guest : stay.getAdditionalGuests()) {
                    System.out.print(guest.getFirstName() + " " + guest.getLastName() + " ");
                }
                System.out.print("check-in date: " + stay.getCheckInDate() + " ");
                System.out.println(
                        "planned check-out date: " + stay.getPlannedCheckOutDate());
            } else {
                System.out.println("Room " + room.getNumber() + " is free.");
            }
        }
    }
}
