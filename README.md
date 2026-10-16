# Climbing Logbook

A command-line logbook for bouldering. Log your sessions (each climb's grade, attempts, and whether you sent it), browse your history, and see how your climbing is progressing over time. Written in Java with a SQLite database.

## Features

- **Log a session** with validated input: V-grade (0-17), attempts (1-99), and sent / not sent. A send on the first attempt is a flash.
- **View history**, newest first, with a one-line summary per session and full detail of a specific session on request.
- **View stats**
  - *Overview:* totals, send and flash rates, average attempts, and send rate by grade
  - *Monthly progress:* sessions per month, your hardest grades each month with send counts and attempts, and send rate by grade over time for a range of grades you choose
  - *Highlights:* hardest send, hardest flash, longest project, and best session
- **Delete all data** to start over (requires typing `DELETE` to confirm).

## Example output

Overview:

```
Sessions: 4
Climbs: 21
Sends: 15
Flashes: 11
Send Rate: 71.4%
Flash Rate: 52.4%
Avg Att per Climb: 2.2
Avg Grade Sent: V4.3

Grade Breakdown
V1   1/1 (100.0%)
V2   1/1 (100.0%)
V4   8/9 (88.9%)
V5   3/4 (75.0%)
V7   1/3 (33.3%)
V8   1/1 (100.0%)
V9   0/2 (0.0%)

```

## Requirements

- Java 17 or newer (developed and tested on Java 25)
- Two small libraries, downloaded in the setup below: the SQLite JDBC driver and SLF4J

## Setup and run

From the project folder:

```bash
# 1. Download the libraries into lib/
mkdir lib
curl -o lib/sqlite-jdbc-3.46.1.0.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar
curl -o lib/slf4j-api-1.7.36.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar

# 2. Compile
javac -cp "lib/*" *.java

# 3. Run
java --enable-native-access=ALL-UNNAMED -cp ".:lib/*" Main
```

The database file `climbing.db` is created in the project folder on first run.

## How it's built

The program is split into layers, where each one only knows about the layer below it:

```
Main               wires everything together
ConsoleUI          menus, prompts, and program flow
ConsolePrinter     all output formatting
InputReader        validated keyboard input (retries until the input is valid)
StatsService       calculations on a list of sessions
ClimbRepository    storage interface
 ├── SqliteRepository      SQLite database (used by default)
 └── TextFileRepository    tab-separated text file
Session, Climb     the data model (no printing, no files)
```

Small records carry results between layers: `GradeSends`, `SendRatio`, `GradeRange`, and `DatedClimb`.

Storage sits behind the `ClimbRepository` interface, with two implementations. Switching from the text file to SQLite was a one-line change in `Main`, because nothing else knows which implementation it's talking to. To use the text file instead, swap the repository line in `Main`.

## Design decisions

- **Validation at every layer.** `InputReader` re-prompts until the input is valid, the `Climb` constructor throws on invalid values so an invalid climb can't exist, and the database has `CHECK` constraints as a last line of defense.
- **Stats are computed, not stored.** Every number comes from the session history on demand, so nothing can drift out of sync.
- **Two tables, linked by a foreign key.** Sessions and climbs are separate tables, so two sessions on the same day stay separate.
- **Transactions on save.** A session and all its climbs are written together or not at all.
- **Hardest sends with counts, not an average grade.** Averages are skewed by warm-ups, and a single V8 send looks the same as five. Showing the top grades each month with how many sends and attempts they took makes progress visible.
- **Best session uses exponential scoring.** Each send is worth 2^grade points, so two V8s beat three V7s, and warm-ups barely count. The base is a constant in `StatsService`.
- **Parameterized queries throughout.** All user text goes into SQL through placeholders, never string concatenation.

## Known limitations

- Climbs aren't linked across sessions, so a project worked over several days shows up as separate entries, and "longest project" means the most attempts in a single entry.
- No editing or deleting of individual sessions (only deleting everything).
- V-scale grades only.
- Testing has been manual: there is no automated test suite yet.

## Ideas for later

- A problems table, so a climb can be tracked across sessions
- Editing and deleting individual sessions
- More stats (TBD)
- A graphical or web interface
