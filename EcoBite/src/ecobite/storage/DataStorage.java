package ecobite.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public class DataStorage<T> {
    private final String storageDirectory;

    public DataStorage(String storageDirectory) {
        this.storageDirectory = resolveStorageDirectory(storageDirectory).toString();
        ensureDirectoryExists();
    }

    private Path resolveStorageDirectory(String directoryName) {
        Path workingDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path projectRoot = findProjectRoot(workingDirectory);
        if (projectRoot == null) {
            CodeSource codeSource = DataStorage.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                try {
                    Path classesLocation = Path.of(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
                    projectRoot = findProjectRoot(Files.isDirectory(classesLocation)
                            ? classesLocation : classesLocation.getParent());
                } catch (URISyntaxException e) {
                    throw new IllegalStateException("Unable to locate the application data directory.", e);
                }
            }
        }
        return (projectRoot == null ? workingDirectory : projectRoot).resolve(directoryName);
    }

    private Path findProjectRoot(Path start) {
        for (Path candidate = start; candidate != null; candidate = candidate.getParent()) {
            if (Files.isDirectory(candidate.resolve("src").resolve("ecobite"))) {
                return candidate;
            }
        }
        return null;
    }

    private void ensureDirectoryExists() {
        File directory = new File(storageDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created && !directory.isDirectory()) {
                throw new IllegalStateException("Failed to create storage directory: "
                        + directory.getAbsolutePath());
            }
        }
        if (!directory.isDirectory()) {
            throw new IllegalStateException("Storage path is not a directory: " + directory.getAbsolutePath());
        }
    }

    public void saveList(String fileName, List<T> items) {
        File file = new File(storageDirectory, fileName);
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(new ArrayList<>(items));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + file.getAbsolutePath() + ": "
                    + e.getMessage(), e);
        }

        if (!file.isFile() || file.length() == 0) {
            throw new IllegalStateException("Data file was not written correctly: " + file.getAbsolutePath());
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> loadList(String fileName) {
        File file = new File(storageDirectory, fileName);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            Object object = inputStream.readObject();
            if (object instanceof List<?>) {
                return (List<T>) object;
            }
            throw new RuntimeException("Unexpected data format in " + file.getAbsolutePath()
                    + ": expected a serialized List.");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load data from " + file.getAbsolutePath() + ": "
                    + e.getMessage(), e);
        }
    }
}
