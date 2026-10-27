# EcoBite

EcoBite is a Java 17 Swing desktop app for rescuing surplus food. It tracks donations, NGO requests, animal-food review, shelter assignments, and completed or discarded food.

## Setup

1. Install Java 17 and MySQL. Keep `lib/mysql-connector-j-8.4.0.jar` beside the project files.
2. For a new database, open `database.sql` in MySQL Workbench and run it once.
3. For an older EcoBite database, back it up and run `migration.sql` once instead. Do not run it on a fresh or already-migrated database.
4. Copy `db.properties.example` to `db.properties`. Set `jdbc.url`, `jdbc.user`, and `jdbc.password`. This local file is ignored by Git.
5. Run `run.bat` on Windows or `./run.sh` on Linux/macOS.
6. The first app launch creates the default account `admin` / `admin123` if it does not exist.

## Food status lifecycle

- New donations start as `AVAILABLE`.
- An NGO request moves human food to `REQUESTED`. Approving completes the request and food; rejecting returns the food to `AVAILABLE`.
- Expired available human food moves to `ANIMAL_REVIEW`. A person can mark it safe for animals (`ANIMAL` + `AVAILABLE`) or discard it.
- Available animal food can be assigned to a shelter. The assignment is recorded and the food becomes `COMPLETED`.

## Tests

Run `test\test.bat` on Windows or `sh test/test.sh` on Linux/macOS. The smoke test checks the DB connection, input rules, password hashing, expiry, request decisions, animal review, shelter assignment, dashboard totals, history, and CSV escaping. It removes only rows created by its run.

## Screenshots

Add screenshots of the sign-in, dashboard, food list, and history screens here.

## Future scope

- Add charts for food saved over time.
- Add a role-based NGO and shelter workflow.
- Add a web or mobile client using the same database.
