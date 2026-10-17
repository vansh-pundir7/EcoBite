# EcoBite - A Two Tier Food Rescue Project

This project is a Java Swing desktop application for managing food waste rescue activities.

## Project structure

- `src/ecobite/Main.java` - application entry point
- `src/ecobite/model` - model classes
- `src/ecobite/ui` - Swing UI classes
- `src/ecobite/ui/UITheme.java` - shared colors, typography, spacing, and Swing component styling
- `src/ecobite/service` - business logic
- `src/ecobite/storage` - local file storage classes
- `data/` - application data files

## Compile

From the project root:

```bash
javac --release 17 -sourcepath src -d out src/ecobite/Main.java
```

## Run

```bash
java -cp out ecobite.Main
```

## Notes

This initial version creates the app shell and shows the main Swing window.
