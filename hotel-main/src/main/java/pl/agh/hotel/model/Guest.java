package pl.agh.hotel.model;

/**
 * Represents a hotel guest identified by a first name and last name.
 * Guest data is immutable after construction.
 */
public class Guest {
    private final String firstName;
    private final String lastName;

    /**
     * Creates a guest with non-blank personal data.
     *
     * @param firstName guest's first name; cannot be null or blank
     * @param lastName guest's last name; cannot be null or blank
     * @throws IllegalArgumentException when either name is null or blank
     */
    public Guest(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                    "First name can't be null or blank.");
        }
        this.firstName = firstName;

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Last name can't be null or blank.");
        }
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
