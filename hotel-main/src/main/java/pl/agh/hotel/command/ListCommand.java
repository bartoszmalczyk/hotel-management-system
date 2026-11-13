package pl.agh.hotel.command;

import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;

import java.util.List;

public class ListCommand implements Command{
    private final Hotel hotel;

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
            if (stay != null) { // room is occupied
                System.out.print("Room " + room.getNumber() + " is occupied, guests: ");
                System.out.print(stay.getMainGuest().getFirstName() + " " + stay.getMainGuest().getLastName() + " ");
                for (Guest guest : stay.getAdditionalGuests()) {
                    System.out.print(guest.getFirstName() + " " + guest.getLastName() + " ");
                }
                System.out.print("check in date: " + stay.getCheckInDate() + " ");
                System.out.println("planned check out date: " + stay.getPlannedCheckOutDate());
            } else { // room is free
                System.out.println("Room " + room.getNumber() + " is free.");
            }

        }
    }
}
