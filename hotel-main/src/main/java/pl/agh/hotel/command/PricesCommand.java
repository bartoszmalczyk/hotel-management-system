package pl.agh.hotel.command;

import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;

/**
 * Displays every configured room and its daily price in grosze.
 */
public class PricesCommand implements Command {
    private final Hotel hotel;

    /**
     * Creates a command operating on the supplied hotel.
     *
     * @param hotel hotel whose prices will be displayed; cannot be null
     * @throws IllegalArgumentException when the hotel is null
     */
    public PricesCommand(Hotel hotel) {
        if (hotel == null) {
            throw new IllegalArgumentException(
                    "Hotel can't be null");
        }

        this.hotel = hotel;
    }

    @Override
    public void execute() {
        for (Room room : hotel.getRooms()) {
            System.out.println(
                    "Room: " + room.getNumber()
                            + ", price in grosze: " + room.getPriceInGrosz());
        }
    }
}
