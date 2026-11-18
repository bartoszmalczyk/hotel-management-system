# Hotel Management System

A Java 21 console application for managing hotel rooms and guest stays. It supports guest check-in, check-out with stay cost calculation, and saving and loading hotel data in CSV format.

Originally developed as part of a Java programming course at AGH University of Krakow, this project demonstrates object-oriented programming, a modular application structure, the Command pattern, and automated testing.

**Author:** Bartosz Malczyk

## Features

- Browse rooms, nightly rates, and current occupancy.
- Check in a primary guest and additional guests with room capacity validation.
- View stay dates, planned check-out dates, and notes.
- Check out guests and calculate charges based on the actual number of days stayed.
- Export the complete hotel state to CSV and restore it on the next launch.
- Validate input and handle errors in the console.

## Technologies and Code Structure

Java 21, Maven, JUnit Jupiter 5, Apache Commons CSV, and JaCoCo. The Maven Shade Plugin builds an executable JAR with dependencies included.

| Module | Responsibility |
| --- | --- |
| `hotel-main` | Hotel model, stay management, console commands, and CSV persistence. |
| `hotel-utils` | A custom `Map<K,V>` interface and its `MyMap<K,V>` implementation. |

The domain model consists of `Hotel`, `Room`, `Guest`, and `Stay`. Console interaction and persistence are separated into dedicated packages. Each command implements the `Command` interface and is registered in `Main`, allowing new operations to be added without changing the console loop.

Date handling uses an injected `Clock`, making tests independent of the day they run. Monetary amounts are stored as integer values in grosze (1 PLN = 100 grosze). CSV import validates all records before returning a hotel instance, preventing the application from using partially loaded data.

`MyMap` was implemented as an exercise in generics and collections. It stores keys and values in two `ArrayList` instances and uses linear lookup. It is used to store rooms, stays, and commands, among other data.

## Getting Started

Requirements: **JDK 21** and **Maven**. Run the following commands from the project root.

```bash
mvn clean verify
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar
```

Without arguments, the application creates three sample rooms: 101, 102, and 201. To load a hotel from a file:

```bash
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar demo-data/hotel-valid.csv
```

Enclose paths containing spaces in quotes. If the file is missing or contains invalid data, the application displays an error message and stops startup.

## Console Commands

Enter a command name, then follow the prompts. The console interface is in English. Command names are case-insensitive.

| Command | Action |
| --- | --- |
| `prices` | List rooms and nightly rates in grosze. |
| `view` | Show details of a selected room and stay. |
| `checkin` | Check in guests. |
| `checkout` | Calculate the charge and make the room available. |
| `list` | List rooms, guests, and stay dates. |
| `save` | Save the hotel state to a specified CSV file. |
| `exit` | Exit the application. |

Dates use the `YYYY-MM-DD` format. During check-in, leaving the date blank selects today; future dates are rejected. The charge is the nightly room rate multiplied by the number of days between check-in and check-out. A stay ending on the day of check-in costs 0 grosze.

### Quick Demo

1. Start the application without arguments and enter `prices`.
2. Enter `checkin`, select room `102`, provide the guest's details, and enter `0` for additional guests. Leave the date blank, enter the planned number of days, and optionally add notes.
3. Inspect the hotel state using `list` and `view`.
4. Enter `save` and use `demo-data/saved-hotel.csv` as the path. Close the application with `exit`.
5. Restart with the saved file and enter `list`:

```bash
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar demo-data/saved-hotel.csv
```

## CSV Format

Files use UTF-8 encoding and commas as separators. The header and column order are fixed:

```csv
recordType,roomNumber,description,priceInGrosz,capacity,firstName,lastName,checkInDate,plannedDays,notes
ROOM,101,Single room,15000,1,,,,,
ROOM,102,Double room,25000,2,Jan,Kowalski,2026-11-10,3,"Late arrival, after 22:00"
GUEST,102,,,,Anna,Nowak,,,
```

A `ROOM` record describes a room and an optional stay for the primary guest. Each additional guest has a separate `GUEST` record. Apache Commons CSV handles fields containing commas, quotes, and line breaks.

The [demo-data](demo-data) directory contains examples of available and occupied rooms, special characters, and intentionally invalid files for exploring input validation.

## Testing and Documentation

```bash
mvn test              # unit tests and the room lifecycle integration test
mvn verify            # tests, executable JAR, JaCoCo reports, and Javadoc
mvn javadoc:javadoc   # generate API documentation only
```

Tests cover the domain model, commands, custom map, CSV import and export, and the complete check-in and check-out lifecycle. They also check invalid input, room capacity limits, date handling, and state preservation when operations are rejected.

After `mvn verify`, JaCoCo reports are available at `hotel-main/target/site/jacoco/index.html` and `hotel-utils/target/site/jacoco/index.html`. API documentation is generated at `javadoc/<module>/apidocs/index.html`.

## Project Scope

The application runs locally in the console and stores data in memory. It does not support future reservations, a database, or concurrent use by multiple users.

Saving is manual: use `save` before exiting to retain changes. Export overwrites the selected file and requires the destination directory to exist. A write failure may leave a partial file.
