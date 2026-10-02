package com.quizplatform.util;

import com.quizplatform.model.QuizAttempt;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for exporting attempt reports.
 * Demonstrates runtime polymorphism where different exporter implementations
 * (CSV, PlainText Summary) are invoked through this common interface.
 */
public interface ReportExporter {

    /**
     * Exports the given quiz attempts to the target file.
     *
     * @param targetFile     File destination
     * @param attempts       List of quiz attempts to export
     * @param quizTitleMap   Lookup map of quiz ID to title
     * @throws IOException   If file I/O fails
     */
    void export(File targetFile, List<QuizAttempt> attempts, Map<Integer, String> quizTitleMap) throws IOException;

    /**
     * Returns the human-readable format identifier.
     */
    String getFormatName();
}
