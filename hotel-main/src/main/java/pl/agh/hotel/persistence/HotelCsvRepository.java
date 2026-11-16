package pl.agh.hotel.persistence;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;
import pl.agh.hotel.utils.Map;
import pl.agh.hotel.utils.MyMap;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes complete hotel snapshots as UTF-8 CSV with ROOM and GUEST records. */
public class HotelCsvRepository {
    private static final String RECORD_TYPE = "recordType";
    private static final String ROOM_NUMBER = "roomNumber";
    private static final String DESCRIPTION = "description";
    private static final String PRICE_IN_GROSZ = "priceInGrosz";
    private static final String CAPACITY = "capacity";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String CHECK_IN_DATE = "checkInDate";
    private static final String PLANNED_DAYS = "plannedDays";
    private static final String NOTES = "notes";

    private static final List<String> EXPECTED_HEADER = List.of(
            RECORD_TYPE,
            ROOM_NUMBER,
            DESCRIPTION,
            PRICE_IN_GROSZ,
            CAPACITY,
            FIRST_NAME,
            LAST_NAME,
            CHECK_IN_DATE,
            PLANNED_DAYS,
            NOTES);

    /**
     * Builds a new hotel only after collecting rooms and their additional guests.
     * Record order does not matter; a failed import never returns a partial hotel.
     *
     * @param path CSV file with the required header
     * @param clock source of today's date for stay validation
     * @return the imported hotel
     * @throws IOException when the file cannot be read or parsed
     * @throws IllegalArgumentException when arguments or CSV data are invalid
     */
    public Hotel load(Path path, Clock clock) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path can't be null.");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock can't be null.");
        }

        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            try (CSVParser parser = format.parse(reader)) {
                List<String> actualHeader = parser.getHeaderNames();
                if (!actualHeader.equals(EXPECTED_HEADER)) {
                    throw new IllegalArgumentException(
                            "Invalid CSV header. Expected: " + EXPECTED_HEADER);
                }
                List<CSVRecord> records = parser.getRecords();
                Map<Integer, RoomData> rooms = new MyMap<>();
                readRooms(records, rooms);
                readGuests(records, rooms);
                return buildHotel(rooms, clock);
            }
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
    }

    /**
     * Writes every room and its current stay using the same schema as {@link #load}.
     * Creates the file or replaces its existing contents. Does not change the hotel.
     *
     * @param hotel hotel to export
     * @param path destination CSV file
     * @throws IOException when writing fails; the destination may be partially written
     * @throws IllegalArgumentException when the hotel or path is null
     */
    public void save(Hotel hotel, Path path) throws IOException {
        if (hotel == null) {
            throw new IllegalArgumentException("Hotel can't be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path can't be null.");
        }

        String[] header = EXPECTED_HEADER.toArray(new String[0]);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(header)
                .get();

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Room room : hotel.getRooms()) {
                Stay stay = hotel.getStay(room.getNumber());
                if (stay == null) {
                    printer.printRecord(
                            "ROOM", room.getNumber(), room.getDescription(),
                            room.getPriceInGrosz(), room.getCapacity(),
                            "", "", "", "", "");
                } else {
                    printer.printRecord(
                            "ROOM", room.getNumber(), room.getDescription(),
                            room.getPriceInGrosz(), room.getCapacity(),
                            stay.getMainGuest().getFirstName(), stay.getMainGuest().getLastName(),
                            stay.getCheckInDate(), stay.getPlannedDays(), stay.getNotes());
                    for (Guest guest : stay.getAdditionalGuests()) {
                        printer.printRecord(
                                "GUEST", room.getNumber(), "", "", "",
                                guest.getFirstName(), guest.getLastName(), "", "", "");
                    }
                }
            }
        }
    }

    private void readRooms(List<CSVRecord> records, Map<Integer, RoomData> rooms) {
        for (CSVRecord record : records) {
            try {
                if (record.size() != EXPECTED_HEADER.size()) {
                    throw new IllegalArgumentException("Expected "
                            + EXPECTED_HEADER.size() + " columns, found " + record.size());
                }
                String type = record.get(RECORD_TYPE).trim();
                if (type.equals("ROOM")) {
                    RoomData data = readRoom(record);
                    int number = data.room().getNumber();
                    if (rooms.contains(number)) {
                        throw new IllegalArgumentException("Duplicate room number: " + number);
                    }
                    rooms.put(number, data);
                } else if (!type.equals("GUEST")) {
                    throw new IllegalArgumentException("Unknown record type: " + type);
                }
            } catch (IllegalArgumentException | DateTimeException exception) {
                throw recordError(record, exception);
            }
        }
    }

    private RoomData readRoom(CSVRecord record) {
        Room room = new Room(
                Integer.parseInt(record.get(ROOM_NUMBER).trim()),
                record.get(DESCRIPTION),
                Integer.parseInt(record.get(PRICE_IN_GROSZ).trim()),
                Integer.parseInt(record.get(CAPACITY).trim())
        );

        String firstName = record.get(FIRST_NAME).trim();
        String lastName = record.get(LAST_NAME).trim();
        String checkInDateText = record.get(CHECK_IN_DATE).trim();
        String plannedDaysText = record.get(PLANNED_DAYS).trim();
        String notes = record.get(NOTES);

        boolean noStay = firstName.isEmpty()
                && lastName.isEmpty()
                && checkInDateText.isEmpty()
                && plannedDaysText.isEmpty()
                && notes.isEmpty();
        if (noStay) {
            return new RoomData(room, null, null, 0, "", new ArrayList<>());
        }

        if (firstName.isEmpty() || lastName.isEmpty()
                || checkInDateText.isEmpty() || plannedDaysText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Incomplete stay data for room " + room.getNumber());
        }

        Guest mainGuest = new Guest(firstName, lastName);
        LocalDate checkInDate = LocalDate.parse(checkInDateText);
        int plannedDays = Integer.parseInt(plannedDaysText);

        return new RoomData(
                room, mainGuest, checkInDate, plannedDays, notes, new ArrayList<>());
    }

    private void readGuests(List<CSVRecord> records, Map<Integer, RoomData> rooms) {
        for (CSVRecord record : records) {
            if (!record.get(RECORD_TYPE).trim().equals("GUEST")) {
                continue;
            }
            try {
                for (String column : List.of(
                        DESCRIPTION, PRICE_IN_GROSZ, CAPACITY,
                        CHECK_IN_DATE, PLANNED_DAYS, NOTES)) {
                    if (!record.get(column).isEmpty()) {
                        throw new IllegalArgumentException(
                                "GUEST field must be empty: " + column);
                    }
                }
                int number = Integer.parseInt(record.get(ROOM_NUMBER).trim());
                RoomData data = rooms.get(number);
                if (data == null) {
                    throw new IllegalArgumentException("Guest refers to missing room: " + number);
                }
                if (data.mainGuest() == null) {
                    throw new IllegalArgumentException("Guest refers to free room: " + number);
                }
                data.additionalGuests().add(new Guest(
                        record.get(FIRST_NAME).trim(), record.get(LAST_NAME).trim()));
            } catch (IllegalArgumentException exception) {
                throw recordError(record, exception);
            }
        }
    }

    private Hotel buildHotel(Map<Integer, RoomData> rooms, Clock clock) {
        Hotel hotel = new Hotel();
        for (int number : rooms.keys()) {
            RoomData data = rooms.get(number);
            hotel.addRoom(data.room());
            if (data.mainGuest() != null) {
                try {
                    Stay stay = new Stay(
                            data.mainGuest(), data.additionalGuests(), data.checkInDate(),
                            data.plannedDays(), data.notes(), clock);
                    // Ensure the derived end date can be represented by LocalDate.
                    stay.getPlannedCheckOutDate();
                    hotel.checkIn(number, stay);
                } catch (IllegalArgumentException | DateTimeException exception) {
                    throw new IllegalArgumentException(
                            "Invalid stay for room " + number + ": " + exception.getMessage(),
                            exception);
                }
            }
        }
        return hotel;
    }

    private IllegalArgumentException recordError(CSVRecord record, RuntimeException cause) {
        return new IllegalArgumentException(
                "CSV data record " + record.getRecordNumber() + ": " + cause.getMessage(), cause);
    }

    private record RoomData(
            Room room,
            Guest mainGuest,
            LocalDate checkInDate,
            int plannedDays,
            String notes,
            List<Guest> additionalGuests) {
    }
}
