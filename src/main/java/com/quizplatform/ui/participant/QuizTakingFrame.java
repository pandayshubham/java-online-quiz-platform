package com.quizplatform.ui.participant;

import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizAttemptService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Interactive Java Swing screen for taking a quiz with questions, A/B/C/D options,
 * real-time countdown timer, previous/next navigation, question palette, and submission.
 */
public class QuizTakingFrame extends JFrame {

    private final Quiz quiz;
    private final int attemptId;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;
    private final Runnable onCompletedCallback;

    private List<Question> questions = new ArrayList<>();
    private final Map<Integer, List<Option>> questionOptionsMap = new HashMap<>();
    private final Map<Integer, Integer> selectedAnswers = new HashMap<>(); // questionId -> optionId

    private int currentIndex = 0;
    private int remainingSeconds;
    private Timer countdownTimer;

    // UI Components
    private JLabel timerLabel;
    private JLabel progressLabel;
    private JLabel questionTitleLabel;
    private JLabel questionTextLabel;

    private JRadioButton radioA;
    private JRadioButton radioB;
    private JRadioButton radioC;
    private JRadioButton radioD;
    private ButtonGroup optionsGroup;

    private JButton prevButton;
    private JButton nextButton;
    private JButton submitButton;
    private JPanel palettePanel;
    private final List<JButton> paletteButtons = new ArrayList<>();

    public QuizTakingFrame(Quiz quiz, int attemptId, QuestionService questionService,
                           QuizAttemptService attemptService, Runnable onCompletedCallback) {
        this(quiz, attemptId, quiz.getDurationMinutes() * 60, questionService, attemptService, onCompletedCallback);
    }

    public QuizTakingFrame(Quiz quiz, int attemptId, int remainingSeconds, QuestionService questionService,
                           QuizAttemptService attemptService, Runnable onCompletedCallback) {
        this.quiz = quiz;
        this.attemptId = attemptId;
        this.remainingSeconds = remainingSeconds;
        this.questionService = questionService;
        this.attemptService = attemptService;
        this.onCompletedCallback = onCompletedCallback;

        loadQuestionsData();
        initUI();
        startTimer();
    }

    private void loadQuestionsData() {
        this.questions = questionService.getQuestionsByQuizId(quiz.getId());
        for (Question q : questions) {
            List<Option> opts = questionService.getOptionsByQuestionId(q.getId());
            questionOptionsMap.put(q.getId(), opts);
        }
    }

    private void initUI() {
        setTitle("Exam in Progress - " + quiz.getTitle());
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleEarlyExit();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Top Bar
        mainPanel.add(createTopBar(), BorderLayout.NORTH);

        // 2. Center: Question Card & Left Palette
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        centerContainer.add(createPalettePanel(), BorderLayout.WEST);
        centerContainer.add(createQuestionPanel(), BorderLayout.CENTER);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Navigation Bar
        mainPanel.add(createBottomBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        if (!questions.isEmpty()) {
            displayQuestion(0);
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        // Left: Quiz Title
        JLabel titleLbl = new JLabel(quiz.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(new Color(15, 23, 42));
        topBar.add(titleLbl, BorderLayout.WEST);

        // Center: Progress Indicator
        progressLabel = new JLabel("Question 1 of " + questions.size(), SwingConstants.CENTER);
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        progressLabel.setForeground(new Color(79, 70, 229));
        topBar.add(progressLabel, BorderLayout.CENTER);

        // Right: Countdown Timer
        timerLabel = new JLabel("Time: --:--", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        timerLabel.setForeground(new Color(220, 38, 38)); // Red
        updateTimerLabel();
        topBar.add(timerLabel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createPalettePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(0, 8));
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setPreferredSize(new Dimension(170, 0));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel paletteTitle = new JLabel("Questions", SwingConstants.CENTER);
        paletteTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        paletteTitle.setForeground(new Color(71, 85, 105));
        sidePanel.add(paletteTitle, BorderLayout.NORTH);

        palettePanel = new JPanel(new GridLayout(0, 3, 6, 6));
        palettePanel.setOpaque(false);

        for (int i = 0; i < questions.size(); i++) {
            final int index = i;
            JButton btn = new JButton(String.valueOf(i + 1));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(new Color(71, 85, 105));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> displayQuestion(index));
            paletteButtons.add(btn);
            palettePanel.add(btn);
        }

        JScrollPane scroll = new JScrollPane(palettePanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        sidePanel.add(scroll, BorderLayout.CENTER);

        return sidePanel;
    }

    private JPanel createQuestionPanel() {
        JPanel qPanel = new JPanel(new BorderLayout(0, 15));
        qPanel.setBackground(Color.WHITE);
        qPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        // Question header & text
        JPanel qHeader = new JPanel();
        qHeader.setLayout(new BoxLayout(qHeader, BoxLayout.Y_AXIS));
        qHeader.setOpaque(false);

        questionTitleLabel = new JLabel("Question 1:");
        questionTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        questionTitleLabel.setForeground(new Color(79, 70, 229));

        questionTextLabel = new JLabel("Question text loading...");
        questionTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionTextLabel.setForeground(new Color(15, 23, 42));

        qHeader.add(questionTitleLabel);
        qHeader.add(Box.createRigidArea(new Dimension(0, 8)));
        qHeader.add(questionTextLabel);
        qPanel.add(qHeader, BorderLayout.NORTH);

        // Options Panel
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        optionsGroup = new ButtonGroup();
        radioA = createOptionRadio();
        radioB = createOptionRadio();
        radioC = createOptionRadio();
        radioD = createOptionRadio();

        optionsGroup.add(radioA);
        optionsGroup.add(radioB);
        optionsGroup.add(radioC);
        optionsGroup.add(radioD);

        optionsPanel.add(radioA);
        optionsPanel.add(radioB);
        optionsPanel.add(radioC);
        optionsPanel.add(radioD);

        qPanel.add(optionsPanel, BorderLayout.CENTER);

        return qPanel;
    }

    private JRadioButton createOptionRadio() {
        JRadioButton radio = new JRadioButton();
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        radio.setForeground(new Color(30, 41, 59));
        radio.setOpaque(false);
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radio.addActionListener(e -> recordSelectedAnswer());
        return radio;
    }

    private JPanel createBottomBar() {
        JPanel btmBar = new JPanel(new BorderLayout());
        btmBar.setOpaque(false);

        prevButton = new JButton("← Previous");
        prevButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prevButton.setPreferredSize(new Dimension(130, 40));
        prevButton.setBackground(new Color(241, 245, 249));
        prevButton.setForeground(new Color(71, 85, 105));
        prevButton.setFocusPainted(false);
        prevButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevButton.addActionListener(e -> {
            if (currentIndex > 0) {
                displayQuestion(currentIndex - 1);
            }
        });

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightGroup.setOpaque(false);

        nextButton = new JButton("Next →");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nextButton.setPreferredSize(new Dimension(120, 40));
        nextButton.setBackground(new Color(79, 70, 229));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextButton.addActionListener(e -> {
            if (currentIndex < questions.size() - 1) {
                displayQuestion(currentIndex + 1);
            }
        });

        submitButton = new JButton("Submit Quiz");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitButton.setPreferredSize(new Dimension(140, 40));
        submitButton.setBackground(new Color(16, 185, 129)); // Green
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> confirmAndSubmitQuiz());

        rightGroup.add(nextButton);
        rightGroup.add(submitButton);

        btmBar.add(prevButton, BorderLayout.WEST);
        btmBar.add(rightGroup, BorderLayout.EAST);

        return btmBar;
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        this.currentIndex = index;

        Question q = questions.get(index);
        progressLabel.setText("Question " + (index + 1) + " of " + questions.size());
        questionTitleLabel.setText("Question " + (index + 1) + " (Order #" + q.getQuestionOrder() + "):");
        questionTextLabel.setText("<html><body style='width: 540px;'>" + q.getQuestionText() + "</body></html>");

        List<Option> opts = questionOptionsMap.get(q.getId());
        optionsGroup.clearSelection();

        Integer previouslySelectedOptId = selectedAnswers.get(q.getId());

        JRadioButton[] radios = {radioA, radioB, radioC, radioD};
        for (int i = 0; i < 4; i++) {
            if (opts != null && i < opts.size()) {
                Option opt = opts.get(i);
                radios[i].setText("  (" + opt.getOptionLabel() + ")  " + opt.getOptionText());
                radios[i].putClientProperty("optionId", opt.getId());
                radios[i].setVisible(true);

                if (previouslySelectedOptId != null && previouslySelectedOptId == opt.getId()) {
                    radios[i].setSelected(true);
                }
            } else {
                radios[i].setVisible(false);
            }
        }

        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < questions.size() - 1);

        updatePaletteVisuals();
    }

    private void recordSelectedAnswer() {
        if (currentIndex < 0 || currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);

        JRadioButton[] radios = {radioA, radioB, radioC, radioD};
        for (JRadioButton rb : radios) {
            if (rb.isSelected()) {
                Integer optId = (Integer) rb.getClientProperty("optionId");
                if (optId != null) {
                    selectedAnswers.put(q.getId(), optId);
                }
                break;
            }
        }

        updatePaletteVisuals();
    }

    private void updatePaletteVisuals() {
        for (int i = 0; i < questions.size(); i++) {
            JButton btn = paletteButtons.get(i);
            Question q = questions.get(i);
            boolean isAnswered = selectedAnswers.containsKey(q.getId());
            boolean isCurrent = (i == currentIndex);

            if (isCurrent) {
                btn.setBackground(new Color(79, 70, 229));
                btn.setForeground(Color.WHITE);
            } else if (isAnswered) {
                btn.setBackground(new Color(220, 252, 231)); // Soft green
                btn.setForeground(new Color(22, 101, 52));
            } else {
                btn.setBackground(new Color(241, 245, 249));
                btn.setForeground(new Color(71, 85, 105));
            }
        }
    }

    private void startTimer() {
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerLabel();

            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                executeSubmission(true);
            }
        });
        countdownTimer.start();
    }


    private void updateTimerLabel() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("Time Remaining: %02d:%02d", mins, secs));

        if (remainingSeconds <= 60) {
            timerLabel.setForeground(new Color(239, 68, 68)); // Flashing/Red in last minute
        }
    }

    private void confirmAndSubmitQuiz() {
        int answeredCount = selectedAnswers.size();
        int total = questions.size();

        String message = String.format("You have answered %d of %d questions.\nAre you sure you want to finish and submit the quiz?",
                answeredCount, total);

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Quiz Submission",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) countdownTimer.stop();
            executeSubmission(false);
        }
    }

    private void executeSubmission(boolean timeExpired) {
        if (countdownTimer != null) countdownTimer.stop();

        try {
            QuizAttempt completedAttempt = attemptService.submitAttempt(attemptId, selectedAnswers, timeExpired);

            dispose();

            Frame parentFrame = null;
            QuizResultDialog resultDialog = new QuizResultDialog(parentFrame, completedAttempt, quiz, questionService, attemptService);
            resultDialog.setVisible(true);

            if (onCompletedCallback != null) {
                onCompletedCallback.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error submitting quiz: " + ex.getMessage(), "Submission Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEarlyExit() {
        Object[] options = {"Continue Quiz", "Exit Quiz"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Your quiz is still in progress. Are you sure you want to exit?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) { // "Exit Quiz" selected
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            dispose();
            if (onCompletedCallback != null) {
                onCompletedCallback.run();
            }
        }
    }
}
