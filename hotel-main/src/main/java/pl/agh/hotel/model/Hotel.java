package pl.agh.hotel.model;
import pl.agh.hotel.utils.Map;
import pl.agh.hotel.utils.MyMap;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private final Map<Integer, Room> rooms = new MyMap<>();
    private final Map<Integer, Stay> stays = new MyMap<>();

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException(
                    "The room can't be null.");
        }
        if (rooms.contains(room.getNumber())) {
            throw new IllegalArgumentException(
                    "The room has been already added.");
        }
        rooms.put(room.getNumber(), room);
    }

    public Room getRoom(int number) {
        Room room = rooms.get(number);
        if (room == null) {
            throw new IllegalArgumentException(
                    "A room with this number doesn't exist.");
        }
        return room;
    }
    public Stay getStay(int number) {
        getRoom(number);
        return stays.get(number);
    }

    public void checkIn(int number, Stay stay) {
        if (stay == null) {
            throw  new IllegalArgumentException(
                    "Stay can't be null");
        }
        if (getStay(number) != null) {
            throw new IllegalStateException(
                    "Room is occupied");
        } // after that we know that room is free
        Room room = getRoom(number);
        int peopleToStay = stay.getAdditionalGuests().size() + 1;
        int roomMaximum = room.getCapacity();
        if (peopleToStay > roomMaximum) {
            throw new IllegalArgumentException(
                    "Number of people can't exceed the room capacity.");
        }
        stays.put(number, stay);
    }

    public long checkOut(int number, Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock can't be null");
        }
        Stay stay = getStay(number);
        if (stay == null) {
            throw new IllegalStateException("Room is not occupied");
        }
        LocalDate checkInDate  = stay.getCheckInDate();
        LocalDate checkOutDate = LocalDate.now(clock);
        long numberOfDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfDays < 0) {
            throw new IllegalArgumentException("Number of days cannot be negative.");
        }
        long price = getRoom(number).getPriceInGrosz();
        long totalPrice = Math.multiplyExact(price, numberOfDays);
        stays.remove(number);
        return totalPrice;
    }

    public List<Room> getRooms() {
        List<Room> allRooms = new ArrayList<>();
        for(int roomNumber : rooms.keys()) {
            allRooms.add(rooms.get(roomNumber));
        }
        return allRooms;
    }
}
