package pl.agh.hotel.command;

import pl.agh.hotel.console.ConsoleApplication;

public class ExitCommand implements Command {
    private final ConsoleApplication application;

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