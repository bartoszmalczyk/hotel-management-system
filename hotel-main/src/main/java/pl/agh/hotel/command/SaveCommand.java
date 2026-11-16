package pl.agh.hotel.command;

import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.persistence.HotelCsvRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/** Reads a destination path and exports every room and current stay to CSV. */
public class SaveCommand implements Command {
    private final Hotel hotel;
    private final Scanner scanner;
    private final HotelCsvRepository repository;

    /**
     * Creates a save command using the shared console scanner.
     *
     * @param hotel hotel to export
     * @param scanner input source for the destination path
     * @param repository CSV writer
     * @throws IllegalArgumentException when any dependency is null
     */
    public SaveCommand(Hotel hotel, Scanner scanner, HotelCsvRepository repository) {
        if (hotel == null) {
            throw new IllegalArgumentException("Hotel can't be null.");
        }
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner can't be null.");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository can't be null.");
        }
        this.hotel = hotel;
        this.scanner = scanner;
        this.repository = repository;
    }

    @Override
    public void execute() {
        System.out.println("Enter output CSV path:");
        if (!scanner.hasNextLine()) {
            System.out.println("Could not save hotel: no path provided.");
            return;
        }
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Path can't be blank.");
            }
            repository.save(hotel, Path.of(input));
            System.out.println("Hotel saved successfully.");
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Could not save hotel: " + exception.getMessage());
        }
    }
}
