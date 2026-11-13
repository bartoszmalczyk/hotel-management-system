package pl.agh.hotel.command;
import pl.agh.hotel.model.Hotel;
import pl.agh.hotel.model.Room;

public class PricesCommand implements Command {
    private final Hotel hotel;
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
            System.out.println("Room: " + room.getNumber() + ", Price in grosze: " + room.getPriceInGrosz());
        }
    }
}
