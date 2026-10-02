package com.quizplatform.util;

import com.quizplatform.model.QuizAttempt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Standard Java CSV Export Utility.
 * Uses pure Java I/O with UTF-8 encoding and robust RFC 4180 escaping.
 * No external dependencies required.
 */
public class CsvExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Escapes a single CSV value according to RFC 4180 rules.
     */
    public static String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        boolean containsSpecialChar = str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r");
        if (containsSpecialChar) {
            str = str.replace("\"", "\"\"");
            return "\"" + str + "\"";
        }
        return str;
    }

    /**
     * Exports participant attempt history to CSV.
     */
    public static void exportParticipantHistory(File targetFile, List<QuizAttempt> attempts, Map<Integer, String> quizTitleMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            // Write UTF-8 BOM for Excel compatibility
            writer.write("\uFEFF");

            // Header
            writer.write("Attempt ID,Quiz ID,Quiz Title,Score,Total Questions,Correct Answers,Incorrect Answers,Unanswered Questions,Percentage,Status,Started At,Completed At\n");

            for (QuizAttempt a : attempts) {
                String quizTitle = quizTitleMap != null ? quizTitleMap.getOrDefault(a.getQuizId(), "Quiz #" + a.getQuizId()) : "Quiz #" + a.getQuizId();
                String started = a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "";
                String completed = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) : "";

                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(a.getId()),
                        escapeCsv(a.getQuizId()),
                        escapeCsv(quizTitle),
                        escapeCsv(a.getScore()),
                        escapeCsv(a.getTotalQuestions()),
                        escapeCsv(a.getCorrectAnswers()),
                        escapeCsv(a.getIncorrectAnswers()),
                        escapeCsv(a.getUnansweredQuestions()),
                        escapeCsv(String.format("%.1f%%", a.getPercentage())),
                        escapeCsv(a.getStatus().name()),
                        escapeCsv(started),
                        escapeCsv(completed)
                ));
            }
        }
    }

    /**
     * Exports quiz creator participant results to CSV.
     */
    public static void exportCreatorResults(File targetFile, List<QuizAttempt> attempts,
                                            Map<Integer, String> quizTitleMap,
                                            Map<Integer, String[]> userMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.write("Attempt ID,Quiz Title,Participant Name,Participant Email,Score,Total Questions,Correct Answers,Incorrect Answers,Unanswered Questions,Percentage,Status,Date Taken\n");

            for (QuizAttempt a : attempts) {
                String quizTitle = quizTitleMap != null ? quizTitleMap.getOrDefault(a.getQuizId(), "Quiz #" + a.getQuizId()) : "Quiz #" + a.getQuizId();
                String[] userInfo = userMap != null ? userMap.get(a.getParticipantId()) : null;
                String partName = (userInfo != null && userInfo.length > 0) ? userInfo[0] : ("User #" + a.getParticipantId());
                String partEmail = (userInfo != null && userInfo.length > 1) ? userInfo[1] : "";
                String dateTaken = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) :
                        (a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "");

                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(a.getId()),
                        escapeCsv(quizTitle),
                        escapeCsv(partName),
                        escapeCsv(partEmail),
                        escapeCsv(a.getScore()),
                        escapeCsv(a.getTotalQuestions()),
                        escapeCsv(a.getCorrectAnswers()),
                        escapeCsv(a.getIncorrectAnswers()),
                        escapeCsv(a.getUnansweredQuestions()),
                        escapeCsv(String.format("%.1f%%", a.getPercentage())),
                        escapeCsv(a.getStatus().name()),
                        escapeCsv(dateTaken)
                ));
            }
        }
    }

    /**
     * Exports admin recent attempts overview to CSV.
     */
    public static void exportAdminRecentAttempts(File targetFile, List<QuizAttempt> attempts,
                                                 Map<Integer, String> quizTitleMap,
                                                 Map<Integer, String> creatorNameMap,
                                                 Map<Integer, String> participantNameMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.write("Attempt ID,Participant,Quiz Title,Creator / Instructor,Score,Total Questions,Percentage,Status,Date Taken\n");

            for (QuizAttempt a : attempts) {
                String partName = participantNameMap != null ? participantNameMap.getOrDefault(a.getParticipantId(), "User #" + a.getParticipantId()) : "User #" + a.getParticipantId();
                String quizTitle = quizTitleMap != null ? quizTitleMap.getOrDefault(a.getQuizId(), "Quiz #" + a.getQuizId()) : "Quiz #" + a.getQuizId();
                String creatorName = creatorNameMap != null ? creatorNameMap.getOrDefault(a.getQuizId(), "Unknown") : "Unknown";
                String dateTaken = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) :
                        (a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "");

                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(a.getId()),
                        escapeCsv(partName),
                        escapeCsv(quizTitle),
                        escapeCsv(creatorName),
                        escapeCsv(a.getScore()),
                        escapeCsv(a.getTotalQuestions()),
                        escapeCsv(String.format("%.1f%%", a.getPercentage())),
                        escapeCsv(a.getStatus().name()),
                        escapeCsv(dateTaken)
                ));
            }
        }
    }
}
