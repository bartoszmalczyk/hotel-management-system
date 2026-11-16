package pl.agh.hotel.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import pl.agh.hotel.model.Guest;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;
import pl.agh.hotel.model.Stay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelCsvRepositoryTest {
    private static final String HEADER = "recordType,roomNumber,description,priceInGrosz,"
            + "capacity,firstName,lastName,checkInDate,plannedDays,notes\n";
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T12:00:00Z"), ZoneId.of("Europe/Warsaw"));

    @TempDir
    Path directory;

    @Test
    void loadsFreeAndOccupiedRooms() throws IOException {
        Path file = writeCsv(
                "ROOM,101,Single room,15000,1,,,,,\n"
                        + "ROOM,102,Double room,25000,2,Jan,Kowalski,2026-11-10,3,\"Late arrival, after 22:00\"\n");

        Hotel hotel = new HotelCsvRepository().load(file, CLOCK);

        assertEquals(2, hotel.getRooms().size());
        assertEquals(15000, hotel.getRoom(101).getPriceInGrosz());
        assertNull(hotel.getStay(101));
        Stay stay = hotel.getStay(102);
        assertEquals("Jan", stay.getMainGuest().getFirstName());
        assertEquals("Kowalski", stay.getMainGuest().getLastName());
        assertEquals(LocalDate.of(2026, 11, 10), stay.getCheckInDate());
        assertEquals(3, stay.getPlannedDays());
        assertEquals(LocalDate.of(2026, 11, 13), stay.getPlannedCheckOutDate());
        assertEquals("Late arrival, after 22:00", stay.getNotes());
    }

    @Test
    void rejectsIncompleteStay() throws IOException {
        Path file = writeCsv("ROOM,101,Single room,15000,1,Jan,,,,\n");

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void rejectsNonPositivePlannedDays() throws IOException {
        Path file = writeCsv(
                "ROOM,101,Single room,15000,1,Jan,Kowalski,2026-11-10,0,\n");

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void rejectsFutureCheckInDate() throws IOException {
        Path file = writeCsv(
                "ROOM,101,Single room,15000,1,Jan,Kowalski,2026-11-13,3,\n");

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void loadsAdditionalGuestsRegardlessOfRecordOrder() throws IOException {
        Path file = writeCsv(
                "GUEST,102,,,,Anna,Nowak,,,\n"
                        + "ROOM,102,Family room,25000,3,Jan,Kowalski,2026-11-10,3,\n"
                        + "GUEST,102,,,,Piotr,Kowalski,,,\n");

        Hotel hotel = new HotelCsvRepository().load(file, CLOCK);

        Stay stay = hotel.getStay(102);
        assertEquals(2, stay.getAdditionalGuests().size());
        assertEquals("Anna", stay.getAdditionalGuests().get(0).getFirstName());
        assertEquals("Nowak", stay.getAdditionalGuests().get(0).getLastName());
        assertEquals("Piotr", stay.getAdditionalGuests().get(1).getFirstName());
        assertEquals("Kowalski", stay.getAdditionalGuests().get(1).getLastName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ROOM,101,Duplicate,15000,1,,,,,\n",
            "ROOM,102,Invalid price,0,1,,,,,\n",
            "ROOM,102,Invalid capacity,15000,0,,,,,\n",
            "ROOM,102,Invalid number,abc,1,,,,,\n",
            "ROOM,102,Invalid date,15000,1,Jan,Nowak,2026-02-30,2,\n",
            "ROOM,102,Negative days,15000,1,Jan,Nowak,2026-11-10,-2,\n",
            "ROOM,102,Incomplete,15000,1,,,,,Notes without stay\n",
            "GUEST,999,,,,Anna,Nowak,,,\n",
            "GUEST,101,,,,Anna,Nowak,,,\n",
            "UNKNOWN,102,,,,,,,,\n",
            "ROOM,102,Too few columns\n",
            "ROOM,102,Too many columns,15000,1,,,,,,extra\n"
    })

    void rejectsInvalidRecordAfterValidRoom(String invalidRecord) throws IOException {
        Path file = writeCsv("ROOM,101,Single room,15000,1,,,,,\n" + invalidRecord);

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GUEST,102,,,,Anna,Nowak,,,\n",
            "GUEST,102,,,,,Nowak,,,\n",
            "GUEST,102,Unexpected description,,,Anna,Nowak,,,\n",
            "GUEST,102,,,,Anna,Nowak,,,Unexpected notes\n"
    })
    void rejectsInvalidGuestOrExceededCapacity(String guestRecord) throws IOException {
        Path file = writeCsv(
                "ROOM,102,Single room,15000,1,Jan,Kowalski,2026-11-10,3,\n"
                        + guestRecord);

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void rejectsInvalidHeader() throws IOException {
        Path file = Files.writeString(directory.resolve("bad.csv"), "number,description\n");

        assertThrows(IllegalArgumentException.class,
                () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void rejectsMissingFileAndNullArguments() {
        HotelCsvRepository repository = new HotelCsvRepository();
        Path missingFile = directory.resolve("missing.csv");

        assertThrows(IOException.class, () -> repository.load(missingFile, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> repository.load(null, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> repository.load(missingFile, null));
    }

    @Test
    void rejectsMalformedCsv() throws IOException {
        Path file = writeCsv("ROOM,101,\"Unclosed description\n");

        assertThrows(IOException.class, () -> new HotelCsvRepository().load(file, CLOCK));
    }

    @Test
    void savesEveryRoomAndAdditionalGuestWithAllFields() throws IOException {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(101, "Single room", 15000, 1));
        hotel.addRoom(new Room(102, "Double room", 25000, 2));
        hotel.checkIn(102, new Stay(
                new Guest("Jan", "Kowalski"), List.of(new Guest("Anna", "Nowak")),
                LocalDate.of(2026, 11, 10), 3, "Late arrival", CLOCK));
        Path file = directory.resolve("saved.csv");

        new HotelCsvRepository().save(hotel, file);

        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().get();
        try (CSVParser parser = format.parse(Files.newBufferedReader(file, StandardCharsets.UTF_8))) {
            assertEquals(HEADER.strip(), String.join(",", parser.getHeaderNames()));
            List<CSVRecord> records = parser.getRecords();
            assertEquals(3, records.size());

            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : records) {
                rows.add(record.toList());
            }
            assertTrue(rows.contains(List.of(
                    "ROOM", "101", "Single room", "15000", "1", "", "", "", "", "")));
            assertTrue(rows.contains(List.of(
                    "ROOM", "102", "Double room", "25000", "2", "Jan", "Kowalski",
                    "2026-11-10", "3", "Late arrival")));
            assertTrue(rows.contains(List.of(
                    "GUEST", "102", "", "", "", "Anna", "Nowak", "", "", "")));
        }
    }

    @Test
    void saveAndLoadPreserveRoomsStaysAndSpecialCharacters() throws IOException {
        Hotel hotel = new Hotel();
        hotel.addRoom(new Room(101, "Pokój wolny", 15001, 1));
        hotel.addRoom(new Room(201, "Rodzinny, z \"widokiem\"", 40099, 3));
        Stay stay = new Stay(
                new Guest("Łukasz", "Kowalski"),
                List.of(new Guest("Anna", "Nowak"), new Guest("Piotr", "Wiśniewski")),
                LocalDate.of(2026, 11, 10), 3, "Uwagi, \"ważne\"\ndruga linia", CLOCK);
        hotel.checkIn(201, stay);
        HotelCsvRepository repository = new HotelCsvRepository();
        Path file = directory.resolve("round-trip.csv");

        repository.save(hotel, file);
        Hotel restored = repository.load(file, CLOCK);

        assertEquals(2, restored.getRooms().size());
        for (Room original : hotel.getRooms()) {
            Room copy = restored.getRoom(original.getNumber());
            assertEquals(original.getDescription(), copy.getDescription());
            assertEquals(original.getPriceInGrosz(), copy.getPriceInGrosz());
            assertEquals(original.getCapacity(), copy.getCapacity());
        }

        assertNull(restored.getStay(101));
        Stay copy = restored.getStay(201);
        assertEquals(stay.getMainGuest().getFirstName(), copy.getMainGuest().getFirstName());
        assertEquals(stay.getMainGuest().getLastName(), copy.getMainGuest().getLastName());
        assertEquals(stay.getCheckInDate(), copy.getCheckInDate());
        assertEquals(stay.getPlannedDays(), copy.getPlannedDays());
        assertEquals(stay.getPlannedCheckOutDate(), copy.getPlannedCheckOutDate());
        assertEquals(stay.getNotes(), copy.getNotes());
        assertEquals(2, copy.getAdditionalGuests().size());
        for (int i = 0; i < 2; i++) {
            assertEquals(stay.getAdditionalGuests().get(i).getFirstName(),
                    copy.getAdditionalGuests().get(i).getFirstName());
            assertEquals(stay.getAdditionalGuests().get(i).getLastName(),
                    copy.getAdditionalGuests().get(i).getLastName());
        }
        assertSame(stay, hotel.getStay(201));
    }

    @Test
    void savingEmptyHotelReplacesPreviousFileContentsWithHeader() throws IOException {
        Path file = Files.writeString(directory.resolve("empty.csv"), "old contents");
        HotelCsvRepository repository = new HotelCsvRepository();

        repository.save(new Hotel(), file);

        assertEquals(HEADER.strip(), Files.readString(file).strip());
        assertEquals(0, repository.load(file, CLOCK).getRooms().size());
    }

    @Test
    void saveRejectsNullArgumentsAndReportsWriteFailure() {
        HotelCsvRepository repository = new HotelCsvRepository();
        Hotel hotel = new Hotel();

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null, directory.resolve("null.csv")));
        assertThrows(IllegalArgumentException.class, () -> repository.save(hotel, null));
        assertThrows(IOException.class, () -> repository.save(hotel, directory));
    }

    private Path writeCsv(String records) throws IOException {
        return Files.writeString(
                directory.resolve("hotel.csv"), HEADER + records, StandardCharsets.UTF_8);
    }
}
