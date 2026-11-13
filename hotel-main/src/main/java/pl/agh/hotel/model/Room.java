package pl.agh.hotel.model;

/**
 * Describes a hotel room and its properties. The room number identifies
 * its floor using the first digit, the price is stored in grosze to avoid
 * losing precision.
 */
public class Room {
    private final int number;
    private final String description;
    private final int priceInGrosz;
    private final int capacity;

    /**
     * Creates a room with a unique three-digit number, description, daily
     * price and positive capacity.
     *
     * @param number three-digit room number whose first digit identifies the
     *               floor
     * @param description non-blank description of the room
     * @param priceInGrosz positive daily price of the whole room in grosze
     * @param capacity positive maximum number of guests
     * @throws IllegalArgumentException when the number is not three-digit,
     *         the description is null or blank, or a numeric value is not
     *         positive
     */
    public Room(int number, String description, int priceInGrosz, int capacity) {
        if (number < 100 || number > 999) {
            throw new IllegalArgumentException("Room number must have 3 digits.");
        }
        this.number = number;

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be null or blank.");
        }
        this.description = description;

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.capacity = capacity;

        if (priceInGrosz <= 0) {
            throw new IllegalArgumentException(
                    "Price must be a positive number.");
        }
        this.priceInGrosz = priceInGrosz;
    }

    public int getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getPriceInGrosz() {
        return priceInGrosz;
    }
}
