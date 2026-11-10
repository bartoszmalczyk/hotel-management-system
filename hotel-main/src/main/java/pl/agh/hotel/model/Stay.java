package pl.agh.hotel.model;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class Stay {
    private final Guest mainGuest;
    private final List<Guest> additionalGuests;
    private final LocalDate checkInDate;
    private final int plannedDays;
    private final String notes;

    public Stay(Guest mainGuest, List<Guest> additionalGuests, LocalDate checkInDate, int plannedDays, String notes, Clock clock) {
        if (mainGuest == null) {
            throw new IllegalArgumentException(
                    "Main Guest can't be null.");
        }
        this.mainGuest = mainGuest;

        if (additionalGuests == null) {
            throw new IllegalArgumentException(
                    "Additional Guests can't be null or contain null.");
        }
        for (Guest guest : additionalGuests) {
            if (guest == null) {
                throw new IllegalArgumentException(
                        "Additional Guests can't contain null.");
            }
        }
        this.additionalGuests = List.copyOf(additionalGuests);

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock can't be null");
        }
        if (checkInDate == null || checkInDate.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be null or in future.");
        }
        this.checkInDate = checkInDate;

        if (notes == null) {
            throw new IllegalArgumentException(
                    "Notes can't be null");
        }
        this.notes = notes;

        if (plannedDays <= 0) {
            throw new IllegalArgumentException(
                    "Planned days must be positive");
        }
        this.plannedDays = plannedDays;
    }

    public Guest getMainGuest() {
        return mainGuest;
    }

    public List<Guest> getAdditionalGuests() {
        return additionalGuests;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public int getPlannedDays() {
        return plannedDays;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getPlannedCheckOutDate() {
        return checkInDate.plusDays(plannedDays);
    }
}
