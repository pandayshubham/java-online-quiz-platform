package com.quizplatform.ui.creator;

import com.quizplatform.dao.UserDAO;
import com.quizplatform.dao.impl.UserDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import com.quizplatform.model.report.QuizPerformanceSummary;
import com.quizplatform.service.PerformanceReportService;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.service.QuizService;
import com.quizplatform.service.impl.PerformanceReportServiceImpl;
import com.quizplatform.util.CsvExportUtil;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel showing participant attempts, scores, and question-level performance
 * analytics for quizzes owned by the logged-in creator.
 */
public class ParticipantResultsPanel extends JPanel {

    private final QuizService quizService;
    private final QuizAttemptService attemptService;
    private final PerformanceReportService reportService;
    private final UserDAO userDAO;

    private JComboBox<Quiz> quizSelector;

    // Participant Results Table
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    // Question Performance Table
    private JTable questionTable;
    private DefaultTableModel questionModel;

    // KPI Metrics Labels
    private JLabel totalAttemptsVal;
    private JLabel avgPercentageVal;
    private JLabel highestPercentageVal;
    private JLabel lowestPercentageVal;
    private JLabel completedAttemptsVal;
    private JLabel timeExpiredAttemptsVal;

    private final Map<Integer, User> userCache = new HashMap<>();
    private final Map<Integer, Quiz> quizCache = new HashMap<>();
    private List<QuizAttempt> currentDisplayedAttempts = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ParticipantResultsPanel(QuizService quizService, QuizAttemptService attemptService) {
        this.quizService = quizService;
        this.attemptService = attemptService;
        this.reportService = new PerformanceReportServiceImpl();
        this.userDAO = new UserDAOImpl();

        setLayout(new BorderLayout(0, 14));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        initTopBar();
        initKpiCards();
        initContentTables();

        reloadQuizzes();
        refreshResults();
    }

    private void initTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Creator Performance & Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Inspect student scores, attempt status, and question-level accuracy across your quizzes.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(subLabel);
        topPanel.add(textPanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Quiz:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(lblFilter);

        quizSelector = new JComboBox<>();
        quizSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quizSelector.setPreferredSize(new Dimension(240, 32));
        quizSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Quiz q) {
                    setText("#" + q.getId() + " - " + q.getTitle());
                } else if (value == null) {
                    setText("All My Quizzes");
                }
                return this;
            }
        });
        quizSelector.addActionListener(e -> refreshResults());
        filterPanel.add(quizSelector);

        JButton exportCsvBtn = new JButton("Export Results CSV");
        exportCsvBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportCsvBtn.setBackground(new Color(241, 245, 249));
        exportCsvBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCsvBtn.addActionListener(e -> handleExportResultsCsv());
        filterPanel.add(exportCsvBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            reloadQuizzes();
            refreshResults();
        });
        filterPanel.add(refreshBtn);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initKpiCards() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 6, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 72));

        totalAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        avgPercentageVal = new JLabel("0.0%", SwingConstants.CENTER);
        highestPercentageVal = new JLabel("0.0%", SwingConstants.CENTER);
        lowestPercentageVal = new JLabel("0.0%", SwingConstants.CENTER);
        completedAttemptsVal = new JLabel("0", SwingConstants.CENTER);
        timeExpiredAttemptsVal = new JLabel("0", SwingConstants.CENTER);

        kpiPanel.add(createKpiCard("TOTAL ATTEMPTS", totalAttemptsVal, new Color(124, 58, 237)));
        kpiPanel.add(createKpiCard("AVG PERCENTAGE", avgPercentageVal, new Color(37, 99, 235)));
        kpiPanel.add(createKpiCard("HIGHEST %", highestPercentageVal, new Color(22, 163, 74)));
        kpiPanel.add(createKpiCard("LOWEST %", lowestPercentageVal, new Color(217, 119, 6)));
        kpiPanel.add(createKpiCard("COMPLETED", completedAttemptsVal, new Color(16, 185, 129)));
        kpiPanel.add(createKpiCard("TIME EXPIRED", timeExpiredAttemptsVal, new Color(220, 38, 38)));

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 12));
        centerWrapper.setOpaque(false);
        centerWrapper.add(kpiPanel, BorderLayout.NORTH);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void initContentTables() {
        // Upper Table: Participant Attempts
        JPanel attemptsPanel = new JPanel(new BorderLayout(0, 6));
        attemptsPanel.setOpaque(false);

        JLabel attTitle = new JLabel("Participant Attempts");
        attTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        attTitle.setForeground(new Color(30, 41, 59));
        attemptsPanel.add(attTitle, BorderLayout.NORTH);

        String[] columns = {"Participant Name", "Participant Email", "Quiz", "Attempt Date", "Score", "Percentage", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(32);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.setGridColor(new Color(241, 245, 249));
        resultsTable.setShowHorizontalLines(true);
        resultsTable.setShowVerticalLines(false);

        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected) {
                    String status = value != null ? value.toString() : "";
                    switch (status) {
                        case "COMPLETED" -> {
                            lbl.setForeground(new Color(22, 101, 52));
                            lbl.setBackground(new Color(220, 252, 231));
                        }
                        case "IN_PROGRESS" -> {
                            lbl.setForeground(new Color(29, 78, 216));
                            lbl.setBackground(new Color(219, 234, 254));
                        }
                        default -> {
                            lbl.setForeground(new Color(153, 27, 27));
                            lbl.setBackground(new Color(254, 226, 226));
                        }
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        resultsScroll.getViewport().setBackground(Color.WHITE);
        attemptsPanel.add(resultsScroll, BorderLayout.CENTER);

        // Lower Table: Question-Level Performance
        JPanel questionsPanel = new JPanel(new BorderLayout(0, 6));
        questionsPanel.setOpaque(false);

        JLabel qTitle = new JLabel("Question-Level Performance Analytics");
        qTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qTitle.setForeground(new Color(30, 41, 59));
        questionsPanel.add(qTitle, BorderLayout.NORTH);

        String[] qColumns = {"Question", "Times Answered", "Correct Count", "Incorrect Count", "Unanswered Count", "Accuracy %"};
        questionModel = new DefaultTableModel(qColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        questionTable = new JTable(questionModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(32);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionTable.setGridColor(new Color(241, 245, 249));
        questionTable.setShowHorizontalLines(true);
        questionTable.setShowVerticalLines(false);

        JTableHeader qHeader = questionTable.getTableHeader();
        qHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        qHeader.setBackground(new Color(248, 250, 252));
        qHeader.setForeground(new Color(71, 85, 105));
        qHeader.setPreferredSize(new Dimension(qHeader.getWidth(), 32));

        questionTable.getColumnModel().getColumn(0).setPreferredWidth(320);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        questionTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        questionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        questionTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        questionTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        questionTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane qScroll = new JScrollPane(questionTable);
        qScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        qScroll.getViewport().setBackground(Color.WHITE);
        questionsPanel.add(qScroll, BorderLayout.CENTER);

        // Split pane to host both tables comfortably
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, attemptsPanel, questionsPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        Component centerComp = getComponent(1);
        if (centerComp instanceof JPanel centerPanel) {
            centerPanel.add(splitPane, BorderLayout.CENTER);
        }
    }

    public void reloadQuizzes() {
        User creator = SessionManager.getCurrentUser();
        if (creator == null) return;

        Quiz selected = (Quiz) quizSelector.getSelectedItem();
        quizSelector.removeAllItems();
        quizSelector.addItem(null); // Represents "All My Quizzes"

        List<Quiz> quizzes = quizService.getQuizzesByCreator(creator.getId());
        for (Quiz q : quizzes) {
            quizCache.put(q.getId(), q);
            quizSelector.addItem(q);
        }

        if (selected != null) {
            for (int i = 0; i < quizSelector.getItemCount(); i++) {
                Quiz item = quizSelector.getItemAt(i);
                if (item != null && item.getId() == selected.getId()) {
                    quizSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void refreshResults() {
        User creator = SessionManager.getCurrentUser();
        if (creator == null) return;

        tableModel.setRowCount(0);
        questionModel.setRowCount(0);

        Quiz selectedQuiz = (Quiz) quizSelector.getSelectedItem();

        if (selectedQuiz == null) {
            // All creator quizzes
            CreatorPerformanceSummary summary = reportService.getCreatorSummary(creator.getId());
            totalAttemptsVal.setText(String.valueOf(summary.getTotalAttempts()));
            avgPercentageVal.setText(String.format("%.1f%%", summary.getAveragePercentage()));
            highestPercentageVal.setText(String.format("%.1f%%", summary.getHighestPercentage()));
            lowestPercentageVal.setText(String.format("%.1f%%", summary.getLowestPercentage()));
            completedAttemptsVal.setText(String.valueOf(summary.getCompletedAttempts()));
            timeExpiredAttemptsVal.setText(String.valueOf(summary.getTimeExpiredAttempts()));

            currentDisplayedAttempts = reportService.getCreatorAttempts(creator.getId());
            populateAttemptsTable(currentDisplayedAttempts);

            // Populate all questions for creator quizzes
            List<Quiz> creatorQuizzes = quizService.getQuizzesByCreator(creator.getId());
            for (Quiz q : creatorQuizzes) {
                try {
                    List<QuestionAnalytics> analytics = reportService.getQuizQuestionAnalytics(q.getId(), creator.getId());
                    populateQuestionTable(analytics, q.getTitle());
                } catch (Exception ex) {
                    // Ignore
                }
            }
        } else {
            // Selected Quiz
            try {
                QuizPerformanceSummary qs = reportService.getCreatorQuizPerformance(creator.getId(), selectedQuiz.getId());
                totalAttemptsVal.setText(String.valueOf(qs.getTotalAttempts()));
                avgPercentageVal.setText(String.format("%.1f%%", qs.getAveragePercentage()));
                highestPercentageVal.setText(String.valueOf(qs.getHighestScore()));
                lowestPercentageVal.setText(String.valueOf(qs.getLowestScore()));
                completedAttemptsVal.setText(String.valueOf(qs.getCompletedAttempts()));
                timeExpiredAttemptsVal.setText(String.valueOf(qs.getTimeExpiredAttempts()));

                currentDisplayedAttempts = attemptService.getAttemptsByQuiz(selectedQuiz.getId());
                populateAttemptsTable(currentDisplayedAttempts);
                populateQuestionTable(qs.getQuestionAnalytics(), null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to load quiz performance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateAttemptsTable(List<QuizAttempt> attempts) {
        for (QuizAttempt a : attempts) {
            Quiz quiz = quizCache.computeIfAbsent(a.getQuizId(), id -> quizService.getQuizById(id));
            String quizTitle = quiz != null ? quiz.getTitle() : ("Quiz #" + a.getQuizId());

            User participant = userCache.computeIfAbsent(a.getParticipantId(), id -> userDAO.findById(id));
            String pName = participant != null ? participant.getName() : ("Participant #" + a.getParticipantId());
            String pEmail = participant != null ? participant.getEmail() : "N/A";

            String dateStr = a.getCompletedAt() != null ? a.getCompletedAt().format(DATE_FORMATTER) :
                    (a.getStartedAt() != null ? a.getStartedAt().format(DATE_FORMATTER) : "N/A");

            tableModel.addRow(new Object[]{
                    pName,
                    pEmail,
                    quizTitle,
                    dateStr,
                    a.getScore(),
                    String.format("%.1f%%", a.getPercentage()),
                    a.getStatus().name()
            });
        }
    }

    private void populateQuestionTable(List<QuestionAnalytics> analytics, String quizPrefix) {
        for (QuestionAnalytics qa : analytics) {
            String qText = quizPrefix != null ? ("[" + quizPrefix + "] " + qa.getQuestionText()) : qa.getQuestionText();
            questionModel.addRow(new Object[]{
                    qText,
                    qa.getTimesAnswered(),
                    qa.getCorrectCount(),
                    qa.getIncorrectCount(),
                    qa.getUnansweredCount(),
                    String.format("%.1f%%", qa.getAccuracyPercentage())
            });
        }
    }

    private void handleExportResultsCsv() {
        if (currentDisplayedAttempts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No participant results available to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Results CSV");
        fileChooser.setSelectedFile(new File("Participant_Results.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                Map<Integer, String> titleMap = new HashMap<>();
                Map<Integer, String[]> userInfoMap = new HashMap<>();

                for (QuizAttempt a : currentDisplayedAttempts) {
                    Quiz q = quizCache.computeIfAbsent(a.getQuizId(), id -> quizService.getQuizById(id));
                    if (q != null) titleMap.put(q.getId(), q.getTitle());

                    User u = userCache.computeIfAbsent(a.getParticipantId(), id -> userDAO.findById(id));
                    if (u != null) {
                        userInfoMap.put(u.getId(), new String[]{u.getName(), u.getEmail()});
                    }
                }

                CsvExportUtil.exportCreatorResults(fileToSave, currentDisplayedAttempts, titleMap, userInfoMap);
                JOptionPane.showMessageDialog(this, "Participant results exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
