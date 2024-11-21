package org.example.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MigrationResult {
    private final String migrationName;
    private final boolean success;
    private final String message;


}
