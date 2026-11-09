package pl.agh.hotel.model;

public class Room {
    private final int number;
    private final String description;
    private final int priceInGrosz;
    private final int capacity;

    public Room(int number, String description, int priceInGrosz, int capacity) {
        // I assume that room number must have 3 digits:
        // 1st for floor and 2nd 3rd for nbr. [README!]
        if (number < 100 || number > 999) {
            throw new IllegalArgumentException("Room number must have 3 digits.");
        }
        this.number = number;

        if (description == null || description.isBlank() ) {
            throw new IllegalArgumentException("Description cannot be null or  blank");
        }
        this.description = description;

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;

        if (priceInGrosz <= 0) {
            throw new IllegalArgumentException("Price must be a positive number");
        }
        this.priceInGrosz = priceInGrosz;
    }

    public int getNumber() {
        return number;
    }

    public  String getDescription() {
        return  description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getPriceInGrosz() {
        return  priceInGrosz;
    }


}
