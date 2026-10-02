package com.quizplatform.service.impl;

import com.quizplatform.dao.AnswerDAO;
import com.quizplatform.dao.OptionDAO;
import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.AnswerDAOImpl;
import com.quizplatform.dao.impl.OptionDAOImpl;
import com.quizplatform.dao.impl.QuestionDAOImpl;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Answer;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import com.quizplatform.model.report.QuestionResultDetail;
import com.quizplatform.model.report.QuizPerformanceSummary;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.util.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PerformanceReportService.
 */
public class PerformanceReportServiceImpl implements PerformanceReportService {

    private final QuizAttemptDAO attemptDAO;
    private final QuizDAO quizDAO;
    private final QuestionDAO questionDAO;
    private final OptionDAO optionDAO;
    private final AnswerDAO answerDAO;
    private final UserDAO userDAO;

    public PerformanceReportServiceImpl() {
        this(new QuizAttemptDAOImpl(), new QuizDAOImpl(), new QuestionDAOImpl(),
             new OptionDAOImpl(), new AnswerDAOImpl(), new UserDAOImpl());
    }

    public PerformanceReportServiceImpl(QuizAttemptDAO attemptDAO, QuizDAO quizDAO, QuestionDAO questionDAO,
                                        OptionDAO optionDAO, AnswerDAO answerDAO, UserDAO userDAO) {
        this.attemptDAO = attemptDAO;
        this.quizDAO = quizDAO;
        this.questionDAO = questionDAO;
        this.optionDAO = optionDAO;
        this.answerDAO = answerDAO;
        this.userDAO = userDAO;
    }

    private void checkParticipantAuth(int participantId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.PARTICIPANT) {
                if (currentUser.getId() != participantId) {
                    throw new SecurityException("Access Denied: You cannot view performance for another participant.");
                }
            }
        }
    }

    private void checkCreatorAuth(int creatorId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                if (currentUser.getId() != creatorId) {
                    throw new SecurityException("Access Denied: You cannot view reports for another quiz creator.");
                }
            }
        }
    }

    private void checkAdminAuth() {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                throw new SecurityException("Access Denied: Platform overview requires Administrator privileges.");
            }
        }
    }

    @Override
    public ParticipantPerformanceSummary getParticipantSummary(int participantId) {
        checkParticipantAuth(participantId);

        int totalAttempts = attemptDAO.getAttemptsCountByParticipant(participantId);
        int completedAttempts = attemptDAO.getCompletedAttemptsCountByParticipant(participantId);
        int timeExpiredAttempts = attemptDAO.getTimeExpiredAttemptsCountByParticipant(participantId);
        double avgPercentage = attemptDAO.getAveragePercentageByParticipant(participantId);
        int bestScore = attemptDAO.getHighestScoreByParticipant(participantId);

        // Average score
        List<QuizAttempt> attempts = attemptDAO.findByParticipantId(participantId);
        double totalScore = 0;
        int evaluatedCount = 0;
        for (QuizAttempt a : attempts) {
            if (a.getStatus() == AttemptStatus.COMPLETED || a.getStatus() == AttemptStatus.TIME_EXPIRED) {
                totalScore += a.getScore();
                evaluatedCount++;
            }
        }
        double avgScore = evaluatedCount > 0 ? (totalScore / evaluatedCount) : 0.0;

        int totalAnswered = answerDAO.getTotalAnswersCountByParticipant(participantId);
        int totalCorrect = answerDAO.getTotalCorrectAnswersByParticipant(participantId);
        int totalIncorrect = answerDAO.getTotalIncorrectAnswersByParticipant(participantId);
        int totalUnanswered = answerDAO.getTotalUnansweredByParticipant(participantId);

        return new ParticipantPerformanceSummary(
                participantId,
                totalAttempts,
                completedAttempts,
                timeExpiredAttempts,
                Math.round(avgScore * 10.0) / 10.0,
                bestScore,
                totalAnswered,
                totalCorrect,
                totalIncorrect,
                totalUnanswered,
                Math.round(avgPercentage * 10.0) / 10.0
        );
    }

    @Override
    public List<QuizAttempt> getParticipantHistory(int participantId) {
        checkParticipantAuth(participantId);
        return attemptDAO.findByParticipantId(participantId);
    }

    @Override
    public AttemptPerformanceDetail getAttemptPerformance(int attemptId, int participantId) {
        QuizAttempt attempt = attemptDAO.findById(attemptId);
        if (attempt == null) {
            throw new IllegalArgumentException("Attempt not found with id: " + attemptId);
        }

        checkParticipantAuth(participantId);
        if (attempt.getParticipantId() != participantId) {
            if (SessionManager.isLoggedIn()) {
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Attempt does not belong to specified participant.");
                }
            }
        }

        Quiz quiz = quizDAO.findById(attempt.getQuizId());
        String quizTitle = quiz != null ? quiz.getTitle() : ("Quiz #" + attempt.getQuizId());

        List<Question> questions = questionDAO.findByQuizId(attempt.getQuizId());
        List<Answer> answers = answerDAO.findByAttemptId(attemptId);
        Map<Integer, Answer> answerMap = new HashMap<>();
        for (Answer ans : answers) {
            answerMap.put(ans.getQuestionId(), ans);
        }

        List<QuestionResultDetail> details = new ArrayList<>();
        for (Question q : questions) {
            Answer ans = answerMap.get(q.getId());
            Option correctOpt = optionDAO.findCorrectOptionByQuestionId(q.getId());
            String correctLabel = correctOpt != null ? String.valueOf(correctOpt.getOptionLabel()) : "-";
            String correctText = correctOpt != null ? correctOpt.getOptionText() : "-";

            String selectedLabel = "-";
            String selectedText = "Unanswered";
            String resultStr = "UNANSWERED";

            if (ans != null && ans.getSelectedOptionId() != null) {
                Option selectedOpt = optionDAO.findById(ans.getSelectedOptionId());
                if (selectedOpt != null) {
                    selectedLabel = String.valueOf(selectedOpt.getOptionLabel());
                    selectedText = selectedOpt.getOptionText();
                }
                resultStr = ans.isCorrect() ? "CORRECT" : "INCORRECT";
            }

            details.add(new QuestionResultDetail(
                    q.getId(),
                    q.getQuestionOrder(),
                    q.getQuestionText(),
                    q.getExplanation(),
                    selectedLabel,
                    selectedText,
                    correctLabel,
                    correctText,
                    resultStr
            ));
        }

        return new AttemptPerformanceDetail(
                attempt.getId(),
                attempt.getQuizId(),
                quizTitle,
                attempt.getCompletedAt() != null ? attempt.getCompletedAt() : attempt.getStartedAt(),
                attempt.getStatus().name(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getIncorrectAnswers(),
                attempt.getUnansweredQuestions(),
                attempt.getPercentage(),
                details
        );
    }

    @Override
    public QuizPerformanceSummary getParticipantQuizPerformance(int participantId, int quizId) {
        checkParticipantAuth(participantId);

        Quiz quiz = quizDAO.findById(quizId);
        QuizPerformanceSummary summary = new QuizPerformanceSummary();
        summary.setQuizId(quizId);
        summary.setQuizTitle(quiz != null ? quiz.getTitle() : ("Quiz #" + quizId));

        List<QuizAttempt> attempts = attemptDAO.findByParticipantIdAndQuizId(participantId, quizId);
        summary.setAttemptsCount(attempts.size());

        if (attempts.isEmpty()) {
            summary.setBestScore(0);
            summary.setAverageScore(0.0);
            summary.setLatestScore(0);
            summary.setHighestPercentage(0.0);
            summary.setLatestStatus("N/A");
            return summary;
        }

        int bestScore = 0;
        double sumScore = 0;
        int evaluatedCount = 0;
        double highestPct = 0.0;

        for (QuizAttempt a : attempts) {
            if (a.getScore() > bestScore) {
                bestScore = a.getScore();
            }
            if (a.getPercentage() > highestPct) {
                highestPct = a.getPercentage();
            }
            if (a.getStatus() == AttemptStatus.COMPLETED || a.getStatus() == AttemptStatus.TIME_EXPIRED) {
                sumScore += a.getScore();
                evaluatedCount++;
            }
        }

        QuizAttempt latestAttempt = attempts.get(0);
        summary.setBestScore(bestScore);
        summary.setAverageScore(evaluatedCount > 0 ? (Math.round((sumScore / evaluatedCount) * 10.0) / 10.0) : 0.0);
        summary.setLatestScore(latestAttempt.getScore());
        summary.setHighestPercentage(Math.round(highestPct * 10.0) / 10.0);
        summary.setLatestStatus(latestAttempt.getStatus().name());

        return summary;
    }

    @Override
    public CreatorPerformanceSummary getCreatorSummary(int creatorId) {
        checkCreatorAuth(creatorId);

        List<QuizAttempt> attempts = attemptDAO.findByCreatorId(creatorId);
        int totalAttempts = attempts.size();
        int completed = 0;
        int timeExpired = 0;
        double sumPercentage = 0.0;
        double highestPct = 0.0;
        double lowestPct = Double.MAX_VALUE;
        double sumScore = 0.0;
        int highestScore = 0;
        int lowestScore = Integer.MAX_VALUE;
        int evalCount = 0;

        for (QuizAttempt a : attempts) {
            if (a.getStatus() == AttemptStatus.COMPLETED) {
                completed++;
            } else if (a.getStatus() == AttemptStatus.TIME_EXPIRED) {
                timeExpired++;
            }

            if (a.getStatus() == AttemptStatus.COMPLETED || a.getStatus() == AttemptStatus.TIME_EXPIRED) {
                evalCount++;
                sumPercentage += a.getPercentage();
                sumScore += a.getScore();
                if (a.getPercentage() > highestPct) highestPct = a.getPercentage();
                if (a.getPercentage() < lowestPct) lowestPct = a.getPercentage();
                if (a.getScore() > highestScore) highestScore = a.getScore();
                if (a.getScore() < lowestScore) lowestScore = a.getScore();
            }
        }

        if (evalCount == 0) {
            lowestPct = 0.0;
            lowestScore = 0;
        }

        double avgPercentage = evalCount > 0 ? (sumPercentage / evalCount) : 0.0;
        double avgScore = evalCount > 0 ? (sumScore / evalCount) : 0.0;

        return new CreatorPerformanceSummary(
                creatorId,
                totalAttempts,
                completed,
                timeExpired,
                Math.round(avgPercentage * 10.0) / 10.0,
                Math.round(highestPct * 10.0) / 10.0,
                Math.round(lowestPct * 10.0) / 10.0,
                Math.round(avgScore * 10.0) / 10.0,
                highestScore,
                lowestScore
        );
    }

    @Override
    public QuizPerformanceSummary getCreatorQuizPerformance(int creatorId, int quizId) {
        checkCreatorAuth(creatorId);

        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz not found with id: " + quizId);
        }
        if (quiz.getCreatorId() != creatorId) {
            throw new SecurityException("Access Denied: You do not own this quiz.");
        }

        QuizPerformanceSummary summary = new QuizPerformanceSummary();
        summary.setQuizId(quizId);
        summary.setQuizTitle(quiz.getTitle());

        List<QuizAttempt> attempts = attemptDAO.findByQuizId(quizId);
        summary.setTotalAttempts(attempts.size());

        int completed = 0;
        int timeExpired = 0;
        double sumScore = 0;
        double sumPct = 0;
        int highestScore = 0;
        int lowestScore = Integer.MAX_VALUE;
        int evalCount = 0;

        for (QuizAttempt a : attempts) {
            if (a.getStatus() == AttemptStatus.COMPLETED) completed++;
            else if (a.getStatus() == AttemptStatus.TIME_EXPIRED) timeExpired++;

            if (a.getStatus() == AttemptStatus.COMPLETED || a.getStatus() == AttemptStatus.TIME_EXPIRED) {
                evalCount++;
                sumScore += a.getScore();
                sumPct += a.getPercentage();
                if (a.getScore() > highestScore) highestScore = a.getScore();
                if (a.getScore() < lowestScore) lowestScore = a.getScore();
            }
        }

        if (evalCount == 0) lowestScore = 0;

        summary.setCompletedAttempts(completed);
        summary.setTimeExpiredAttempts(timeExpired);
        summary.setAverageScore(evalCount > 0 ? (Math.round((sumScore / evalCount) * 10.0) / 10.0) : 0.0);
        summary.setAveragePercentage(evalCount > 0 ? (Math.round((sumPct / evalCount) * 10.0) / 10.0) : 0.0);
        summary.setHighestScore(highestScore);
        summary.setLowestScore(lowestScore);

        // Question analytics
        summary.setQuestionAnalytics(getQuizQuestionAnalytics(quizId, creatorId));
        return summary;
    }

    @Override
    public List<QuizAttempt> getCreatorAttempts(int creatorId) {
        checkCreatorAuth(creatorId);
        return attemptDAO.findByCreatorId(creatorId);
    }

    @Override
    public List<QuestionAnalytics> getQuizQuestionAnalytics(int quizId, int creatorId) {
        checkCreatorAuth(creatorId);

        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Quiz not found with id: " + quizId);
        }
        if (quiz.getCreatorId() != creatorId) {
            throw new SecurityException("Access Denied: You do not own this quiz.");
        }

        List<Question> questions = questionDAO.findByQuizId(quizId);
        List<QuestionAnalytics> list = new ArrayList<>();

        for (Question q : questions) {
            int timesAnswered = answerDAO.getTimesAnsweredCountByQuestionId(q.getId());
            int correctCount = answerDAO.getCorrectCountByQuestionId(q.getId());
            int incorrectCount = answerDAO.getIncorrectCountByQuestionId(q.getId());
            int unansweredCount = answerDAO.getUnansweredCountByQuestionId(q.getId());

            double accuracy = 0.0;
            if (timesAnswered > 0) {
                accuracy = Math.round(((correctCount * 100.0) / timesAnswered) * 10.0) / 10.0;
            }

            list.add(new QuestionAnalytics(
                    q.getId(),
                    q.getQuestionOrder(),
                    q.getQuestionText(),
                    timesAnswered,
                    correctCount,
                    incorrectCount,
                    unansweredCount,
                    accuracy
            ));
        }

        return list;
    }

    @Override
    public PlatformPerformanceSummary getPlatformSummary() {
        checkAdminAuth();

        int totalAttempts = attemptDAO.getTotalAttemptsCount();
        int completed = attemptDAO.getCompletedAttemptsCount();
        int timeExpired = attemptDAO.getTimeExpiredAttemptsCount();
        double avgScore = attemptDAO.getPlatformAverageScore();
        int totalQuestions = questionDAO.getTotalQuestionsCount();
        int totalAnswers = answerDAO.getTotalAnswersCount();

        return new PlatformPerformanceSummary(
                totalAttempts,
                completed,
                timeExpired,
                Math.round(avgScore * 10.0) / 10.0,
                totalQuestions,
                totalAnswers
        );
    }

    @Override
    public List<QuizAttempt> getRecentPlatformAttempts(int limit) {
        checkAdminAuth();
        return attemptDAO.findRecentAttempts(limit);
    }
}
