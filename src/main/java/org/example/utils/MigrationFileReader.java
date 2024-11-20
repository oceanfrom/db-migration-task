package org.example.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationFileReader {
    private static final String MIGRATION_DIRECTORY = "src/main/resources/db.changelog/versions";

    public static List<File> getMigrationFiles() {
        File migrationDir = new File(MIGRATION_DIRECTORY);
        if(!migrationDir.exists() || !migrationDir.isDirectory()) {
            throw new RuntimeException(MIGRATION_DIRECTORY + " does not exist or is not a directory");
        }

        File[] files = migrationDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files == null) { return List.of(); }

        return Arrays.stream(files)
                .sorted((f1,f2) -> f1.getName().compareTo(f2.getName()))
                .collect(Collectors.toList());
    }

    public static String readSqlFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if(!path.toFile().exists()) {
            throw new RuntimeException(filePath + " does not exist");
        }
        return Files.readString(path);
    }
}
