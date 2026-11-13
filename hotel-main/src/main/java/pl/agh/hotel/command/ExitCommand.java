package pl.agh.hotel.command;

import pl.agh.hotel.console.ConsoleApplication;

/**
 * Stops the console command loop in a controlled manner.
 */
public class ExitCommand implements Command {
    private final ConsoleApplication application;

    /**
     * Creates a command controlling the supplied console application.
     *
     * @param application application to stop; cannot be null
     * @throws IllegalArgumentException when the application is null
     */
    public ExitCommand(ConsoleApplication application) {
        if (application == null) {
            throw new IllegalArgumentException(
                    "Application can't be null");
        }
        this.application = application;
    }

    @Override
    public void execute() {
        application.stop();
        System.out.println("Application closed.");
    }
}
