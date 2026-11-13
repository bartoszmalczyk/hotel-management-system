package pl.agh.hotel.model;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents an active hotel stay, including its guests, check-in date,
 * planned duration and optional notes. The additional guest list is copied
 * during construction so that the stay cannot be changed from outside.
 */
public class Stay {
    private final Guest mainGuest;
    private final List<Guest> additionalGuests;
    private final LocalDate checkInDate;
    private final int plannedDays;
    private final String notes;

    /**
     * Creates a stay whose check-in date is today or in the past according to
     * the supplied clock. A stay must have one main guest and may have any
     * number of additional guests; room capacity is validated by
     * {@link Hotel#checkIn(int, Stay)}.
     *
     * @param mainGuest main guest responsible for the stay; cannot be null
     * @param additionalGuests additional guests; cannot be null or contain
     *                         null elements
     * @param checkInDate actual check-in date; cannot be null or in the future
     * @param plannedDays positive planned number of days
     * @param notes optional notes represented by a non-null string; may be
     *              empty
     * @param clock source of the current date used to validate the check-in
     *              date; cannot be null
     * @throws IllegalArgumentException when any argument violates the stated
     *         contract
     */
    public Stay(
            Guest mainGuest,
            List<Guest> additionalGuests,
            LocalDate checkInDate,
            int plannedDays,
            String notes,
            Clock clock) {
        if (mainGuest == null) {
            throw new IllegalArgumentException(
                    "Main guest can't be null.");
        }
        this.mainGuest = mainGuest;

        if (additionalGuests == null) {
            throw new IllegalArgumentException(
                    "Additional guests can't be null.");
        }
        for (Guest guest : additionalGuests) {
            if (guest == null) {
                throw new IllegalArgumentException(
                        "Additional guests can't contain null.");
            }
        }
        this.additionalGuests = List.copyOf(additionalGuests);

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock can't be null.");
        }
        if (checkInDate == null || checkInDate.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be null or in the future.");
        }
        this.checkInDate = checkInDate;

        if (notes == null) {
            throw new IllegalArgumentException(
                    "Notes can't be null.");
        }
        this.notes = notes;

        if (plannedDays <= 0) {
            throw new IllegalArgumentException(
                    "Planned days must be positive.");
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

    /**
     * Calculates the planned check-out date from the check-in date and planned
     * duration.
     *
     * @return check-in date plus the planned number of days
     */
    public LocalDate getPlannedCheckOutDate() {
        return checkInDate.plusDays(plannedDays);
    }
}
