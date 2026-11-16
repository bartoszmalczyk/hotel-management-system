# Hotel Management System

**Autor:** Bartosz Malczyk, **grupa:** 5.

Konsolowy system zarządzania hotelem. Zrealizowano funkcje podstawowe oraz H6/H7: odczyt i zapis stanu w CSV.

## Budowanie i uruchamianie

Wymagania: JDK 21 i Maven. Polecenia wykonuj z głównego katalogu projektu.

```bash
mvn clean verify
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar
```

Bez argumentu program tworzy pokoje 101, 102 i 201. Import przy starcie:

```bash
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar demo-data/hotel-valid.csv
```

Ścieżka może być bezwzględna lub względna wobec katalogu uruchomienia. Ścieżki ze spacjami podawane jako argument otaczaj cudzysłowami. Brak pliku lub błędny CSV kończy start komunikatem; aplikacja nie uruchamia się na częściowym stanie.

## Komendy

Wielkość liter i spacje wokół nazwy nie mają znaczenia. Błędna komenda nie kończy pętli.

| Komenda | Działanie |
| --- | --- |
| `prices` | Wszystkie pokoje i ceny za dobę w groszach. |
| `view` | Szczegóły wybranego pokoju i pobytu. |
| `checkin` | Zameldowanie głównego i dodatkowych gości. |
| `checkout` | Obliczenie należności i zwolnienie pokoju. |
| `list` | Pokoje, zajętość, goście i daty. |
| `save` | Pytanie o ścieżkę i zapis wszystkich pokoi oraz pobytów do CSV. |
| `exit` | Zakończenie programu. |

Nowa komenda implementuje `Command.execute()` i wymaga jednej rejestracji w `Main` przez `registerCommand()`. Pętla konsoli pozostaje bez zmian.

## Model i moduły

- `hotel-main`: `Room`, `Guest`, `Stay`, `Hotel`, komendy, konsola i `HotelCsvRepository`.
- `hotel-utils`: własny interfejs `Map<K,V>` i `MyMap<K,V>` na dwóch równoległych `ArrayList`.

Pokój ma unikatowy trzycyfrowy numer (pierwsza cyfra oznacza piętro), opis, dodatnią cenę i pojemność. Pobyt zawiera głównego gościa, dodatkowych gości, datę, dodatnią liczbę planowanych dni i opcjonalne uwagi. Liczba osób nie może przekraczać pojemności.

Kwoty są całkowitą liczbą groszy. Koszt całego pokoju to cena za dobę razy rzeczywista różnica dat, niezależnie od planowanej długości pobytu. Ten sam dzień kosztuje 0; ujemny okres jest błędem. Mnożenie używa `Math.multiplyExact`. Daty mają format `YYYY-MM-DD`; przyszłe zameldowanie jest odrzucane. Pusta data w `checkin` oznacza dziś według przekazanego `Clock`. Planowane wymeldowanie oblicza się jako data zameldowania plus `plannedDays`.

`MyMap` porównuje klucze przez `equals`; `put` dodaje lub zastępuje wartość i zwraca `true`. Brak klucza daje `null` w `get` i `false` w `contains`/`remove`. Klucz `null` lub wartość `null` są odrzucane. `keys()` zwraca niezależną listę. Mapa przechowuje pokoje i pobyty w `Hotel`, komendy w konsoli oraz dane tymczasowe importu.

## CSV

UTF-8, separator przecinek, dokładny nagłówek i kolejność 10 kolumn:

```csv
recordType,roomNumber,description,priceInGrosz,capacity,firstName,lastName,checkInDate,plannedDays,notes
ROOM,101,Single room,15000,1,,,,,
ROOM,102,Double room,25000,2,Jan,Kowalski,2026-11-10,3,"Late arrival, after 22:00"
GUEST,102,,,,Anna,Nowak,,,
```

`ROOM` opisuje pokój; puste wszystkie pola pobytu oznaczają wolny pokój. Dla zajętego pokoju wymagane są imię i nazwisko głównego gościa, data i `plannedDays`; uwagi mogą być puste. `GUEST` zawiera tylko typ, numer pokoju, imię i nazwisko dodatkowego gościa; pozostałe pola muszą być puste. Każdy dodatkowy gość ma osobny rekord, a kolejność `ROOM`/`GUEST` jest dowolna.

Apache Commons CSV obsługuje pola z przecinkami, cudzysłowami i nowymi liniami. W polu otoczonym cudzysłowami znak `"` zapisuje się jako `""`. Planowana data wymeldowania nie jest osobną kolumną, ponieważ wynika z daty i liczby dni.

Import odrzuca m.in. zły nagłówek, liczbę kolumn, duplikaty pokoi, niepełne pobyty, błędne liczby i daty, przekroczenie pojemności oraz gościa przypisanego do wolnego lub nieistniejącego pokoju. Cały hotel jest zwracany dopiero po udanym imporcie.

Przykłady w `demo-data/`: `hotel-valid.csv`, `hotel-all-free.csv`, `hotel-full.csv`, `hotel-special-characters.csv` oraz `hotel-invalid*.csv` z celowymi błędami.

Demonstracja H6/H7: uruchom poprawny plik, wykonaj `save`, wpisz `demo-data/saved-hotel.csv`, następnie `exit`. Uruchom **nowy proces**:

```bash
java -jar hotel-main/target/hotel-main-1.0-SNAPSHOT.jar demo-data/saved-hotel.csv
```

Sprawdź `list` i `view`: stan powinien odpowiadać zapisanemu. Restart procesu jest demonstrowany ręcznie; testy automatyczne obejmują zapis i odczyt w jednym procesie.

## Testy i dokumentacja

```bash
mvn test                  # testy jednostkowe i integracyjny cykl pokoju
mvn verify                # dodatkowo JAR, raporty JaCoCo i Javadoc
mvn javadoc:javadoc       # sama dokumentacja
```

122 testy JUnit Jupiter, bez ręcznego wejścia; testy dat używają stałego `Clock`. Raporty do oddania:

- [JaCoCo hotel-main](coverage/hotel-main/index.html), [hotel-utils](coverage/hotel-utils/index.html); XML: `coverage/<moduł>/jacoco.xml`.
- Pokrycie linii łącznie: **495/496 = 99,8%**, bez wyłączeń. Raporty pokazują także pakiety i gałęzie; 
- [Javadoc hotel-main](javadoc/hotel-main/apidocs/index.html), [hotel-utils](javadoc/hotel-utils/apidocs/index.html).
- [SonarQube](sonar-cube/sonarqube.pdf): zrzuty analizy. Serwer nie jest potrzebny do zwykłego budowania.

## Wymagania i konkretne testy

| Kryterium | Kod / dowód |
| --- | --- |
| H1 | `Hotel`, model i komendy; H-T1–H-T5. |
| H2 | `Map`, `MyMap`, rzeczywiste użycie w hotelu; H-T6–H-T7. |
| H3 | Model, `Command`, rejestr `ConsoleApplication`. |
| H4 | Testy w obu modułach i `coverage/`. |
| H5 | POM-y, wykonywalny JAR, README, `javadoc/`, `sonar-cube/`. |
| H6 | `HotelCsvRepository.load`, import w `Main`; H-T8. |
| H7 | `HotelCsvRepository.save`, `SaveCommand`; H-T8. |

Zapis `Klasa#metoda` wskazuje konkretny test w `src/test/java` odpowiedniego modułu.

| Scenariusz | Testy |
| --- | --- |
| H-T1: komendy i błędne wejście | `PricesCommandTest#displaysEveryRoomWithItsPrice`, `ViewCommandTest#reportsNonNumericRoomNumber`, `ViewCommandTest#reportsMissingRoom`, `ListCommandTest#displaysOccupiedRoomWithGuestsAndDates`, `CheckoutCommandTest#checksOutOccupiedRoomAndDisplaysCharge`, `CheckinCommandTest#checksInMainAndAdditionalGuestWithProvidedDate`, `ExitCommandTest#stopsApplicationAndPreventsFollowingCommand`, `ConsoleApplicationTest#executesCommandIgnoringCaseAndSurroundingWhitespace`, `ConsoleApplicationTest#unknownCommandIsReportedAndApplicationContinues`. |
| H-T2: zameldowanie | `CheckinCommandTest#checksInMainAndAdditionalGuestWithProvidedDate`, `CheckinCommandTest#blankDateUsesCurrentDateAndEmptyOptionalDataAreAccepted`, `HotelTest#checkInAssignsStayWhenGuestsFitExactly`. |
| H-T3: odrzucone zameldowanie | `CheckinCommandTest#occupiedRoomIsRejectedWithoutChangingCurrentStay`, `CheckinCommandTest#exceedingCapacityDoesNotOccupyRoom`, `CheckinCommandTest#invalidDateDoesNotOccupyRoom`, `CheckinCommandTest#futureDateDoesNotOccupyRoom`, `CheckinCommandTest#invalidGuestDataDoesNotOccupyRoom`. |
| H-T4: wymeldowanie i koszt | `HotelTest#checkOutCalculatesChargeForActualDaysAndReleasesRoom`, `HotelTest#sameDayCheckOutCostsZeroAndReleasesRoom`, `HotelTest#checkOutCalculatesChargeAcrossMonthBoundary`, `HotelTest#freeAndMissingRoomsCannotBeCheckedOut`, `HotelTest#checkOutBeforeCheckInDateDoesNotReleaseRoom`. |
| H-T5: cykl pokoju | `RoomLifecycleIntegrationTest#completesFullRoomLifecycleWithoutRetainingPreviousStay`. |
| H-T6: odczyt i dodawanie mapy | `MyMapTest#putStoresValueAndMakesKeyPresent`, `MyMapTest#puttingSameKeyTwice`, `MyMapTest#equalKeysAreTreatedAsTheSameKey`. |
| H-T7: usuwanie i ochrona mapy | `MyMapTest#removingKeys`, `MyMapTest#removingFromTheMiddle`, `MyMapTest#modifyingReturnedKeysDoesNotChangeMap`, `MyMapTest#invalidKeysOrValues`. |
| H-T8: CSV | `HotelCsvRepositoryTest#loadsFreeAndOccupiedRooms`, `HotelCsvRepositoryTest#loadsAdditionalGuestsRegardlessOfRecordOrder`, `HotelCsvRepositoryTest#rejectsInvalidRecordAfterValidRoom`, `HotelCsvRepositoryTest#rejectsInvalidGuestOrExceededCapacity`, `HotelCsvRepositoryTest#savesEveryRoomAndAdditionalGuestWithAllFields`, `HotelCsvRepositoryTest#saveAndLoadPreserveRoomsStaysAndSpecialCharacters`, `HotelCsvRepositoryTest#saveRejectsNullArgumentsAndReportsWriteFailure`, `MainTest#invalidFileDoesNotFallBackToDefaultOrPartiallyLoadedRooms`, `SaveCommandTest#reportsWriteFailureWithoutChangingStay`. |

## Ograniczenia

Brak GUI, bazy danych, przyszłych rezerwacji, XLSX i obsługi
wielowątkowej. Dane niezapisane przez `save` znikają po zamknięciu.
Zapis nadpisuje plik, nie tworzy brakujących katalogów i przy błędzie
może pozostawić częściowy plik. Import wczytuje cały CSV do pamięci.

