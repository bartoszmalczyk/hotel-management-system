package pl.agh.hotel.model;

public class Guest {
    private final String firstName;
    private final String lastName;

    public Guest(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()){
            throw new IllegalArgumentException("FirstName can't be null or blank");
        }
        this.firstName = firstName;

        if (lastName == null || lastName.isBlank()){
            throw new IllegalArgumentException("lastName can't be null or blank");
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
