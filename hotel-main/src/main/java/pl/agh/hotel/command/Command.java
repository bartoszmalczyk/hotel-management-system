package pl.agh.hotel.command;

/**
 * Defines a console operation that can be registered and executed by the
 * application command loop.
 */
public interface Command {
    /**
     * Executes one complete invocation of the command and returns control to
     * the command loop when finished.
     */
    void execute();
}
