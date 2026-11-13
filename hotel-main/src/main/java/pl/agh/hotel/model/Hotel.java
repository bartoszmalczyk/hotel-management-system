package pl.agh.hotel.model;

import pl.agh.hotel.utils.Map;
import pl.agh.hotel.utils.MyMap;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages hotel rooms and their current stays. Rooms and stays are stored by
 * room number using the project's {@link MyMap} implementation. The class
 * validates each operation before changing hotel state.
 */
public class Hotel {
    private final Map<Integer, Room> rooms = new MyMap<>();
    private final Map<Integer, Stay> stays = new MyMap<>();

    /**
     * Adds a room without replacing any room already registered under the same
     * number.
     *
     * @param room room to add; cannot be null
     * @throws IllegalArgumentException when the room is null or its number is
     *         already registered
     */
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

    /**
     * Assigns a stay to a free room when all guests fit within its capacity.
     * Validation is completed before the hotel state is changed.
     *
     * @param number number of the room to occupy
     * @param stay stay to assign; cannot be null
     * @throws IllegalArgumentException when the room does not exist, the stay
     *         is null, or the number of guests exceeds room capacity
     * @throws IllegalStateException when the room is already occupied
     */
    public void checkIn(int number, Stay stay) {
        if (stay == null) {
            throw new IllegalArgumentException(
                    "Stay can't be null.");
        }
        if (getStay(number) != null) {
            throw new IllegalStateException(
                    "Room is occupied.");
        }
        Room room = getRoom(number);
        int numberOfGuests = stay.getAdditionalGuests().size() + 1;
        int roomCapacity = room.getCapacity();
        if (numberOfGuests > roomCapacity) {
            throw new IllegalArgumentException(
                    "Number of people can't exceed the room capacity.");
        }
        stays.put(number, stay);
    }

    /**
     * Calculates the charge for the actual number of days between check-in and
     * the current date, then releases the room. A same-day stay costs zero.
     * The room remains occupied if validation or calculation fails.
     *
     * @param number number of the room to release
     * @param clock source of the check-out date; cannot be null
     * @return total charge for the room in grosze
     * @throws IllegalArgumentException when the room does not exist, the clock
     *         is null, or the calculated stay length is negative
     * @throws IllegalStateException when the room is not occupied
     * @throws ArithmeticException when the calculated charge exceeds the
     *         range of {@code long}
     */
    public long checkOut(int number, Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock can't be null.");
        }
        Stay stay = getStay(number);
        if (stay == null) {
            throw new IllegalStateException("Room is not occupied.");
        }
        LocalDate checkInDate = stay.getCheckInDate();
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

    /**
     * Returns all configured rooms in a separate list. Modifying the returned
     * list does not modify the hotel.
     *
     * @return independent list of all rooms
     */
    public List<Room> getRooms() {
        List<Room> allRooms = new ArrayList<>();
        for (int roomNumber : rooms.keys()) {
            allRooms.add(rooms.get(roomNumber));
        }
        return allRooms;
    }
}
