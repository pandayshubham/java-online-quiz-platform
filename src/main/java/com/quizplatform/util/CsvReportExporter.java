package com.quizplatform.util;

import com.quizplatform.model.QuizAttempt;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CSV implementation of ReportExporter.
 * Demonstrates runtime polymorphism.
 */
public class CsvReportExporter implements ReportExporter {

    @Override
    public void export(File targetFile, List<QuizAttempt> attempts, Map<Integer, String> quizTitleMap) throws IOException {
        CsvExportUtil.exportParticipantHistory(targetFile, attempts, quizTitleMap);
    }

    @Override
    public String getFormatName() {
        return "CSV";
    }
}
