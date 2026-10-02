package com.quizplatform.util;

import com.quizplatform.model.QuizAttempt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Plain text summary implementation of ReportExporter.
 * Demonstrates runtime polymorphism.
 */
public class TextSummaryReportExporter implements ReportExporter {

    @Override
    public void export(File targetFile, List<QuizAttempt> attempts, Map<Integer, String> quizTitleMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("====================================================\n");
            writer.write("           QUIZ ATTEMPT SUMMARY REPORT              \n");
            writer.write("====================================================\n\n");
            writer.write(String.format("Total Recorded Attempts: %d\n\n", attempts != null ? attempts.size() : 0));

            if (attempts != null) {
                for (QuizAttempt a : attempts) {
                    String title = quizTitleMap != null ? quizTitleMap.getOrDefault(a.getQuizId(), "Quiz #" + a.getQuizId()) : "Quiz #" + a.getQuizId();
                    writer.write(String.format("Attempt #%d | %s | Score: %d/%d (%.1f%%) | Status: %s\n",
                            a.getId(), title, a.getScore(), a.getTotalQuestions(), a.getPercentage(), a.getStatus()));
                }
            }
            writer.write("\n====================================================\n");
        }
    }

    @Override
    public String getFormatName() {
        return "TEXT";
    }
}
