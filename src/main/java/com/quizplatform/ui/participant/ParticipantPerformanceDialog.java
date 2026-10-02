package com.quizplatform.ui.participant;

import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.model.report.QuestionResultDetail;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Detailed attempt performance dialog showing question-by-question breakdown,
 * participant vs correct answer comparison, explanations, and accuracy visualization.
 */
public class ParticipantPerformanceDialog extends JDialog {

    private final AttemptPerformanceDetail detail;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JTextArea explanationArea;

    public ParticipantPerformanceDialog(Frame owner, AttemptPerformanceDetail detail) {
        super(owner, "Performance Report - Attempt #" + (detail != null ? detail.getAttemptId() : ""), true);
        this.detail = detail;

        setSize(960, 680);
        setMinimumSize(new Dimension(840, 540));
        setLocationRelativeTo(owner);

        initUI();
    }

    private void initUI() {
        if (detail == null) {
            dispose();
            return;
        }

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(248, 250, 252));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Top Header with title and attempt meta
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center: KPI Metrics and Questions Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        centerPanel.add(createMetricsPanel(), BorderLayout.NORTH);
        centerPanel.add(createTableAndExplanationPanel(), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Button Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton closeBtn = new JButton("Close Performance Report");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setPreferredSize(new Dimension(200, 38));
        closeBtn.setBackground(new Color(79, 70, 229));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(detail.getQuizTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(15, 23, 42));

        String dateStr = detail.getAttemptDate() != null ? detail.getAttemptDate().format(DATE_FORMATTER) : "N/A";
        JLabel dateLbl = new JLabel("Attempt #" + detail.getAttemptId() + "  |  Completed: " + dateStr);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(new Color(100, 116, 139));

        leftPanel.add(titleLbl);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftPanel.add(dateLbl);

        header.add(leftPanel, BorderLayout.WEST);

        // Status badge
        JLabel statusBadge = new JLabel(detail.getStatus(), SwingConstants.CENTER);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setPreferredSize(new Dimension(130, 32));

        if ("COMPLETED".equalsIgnoreCase(detail.getStatus())) {
            statusBadge.setBackground(new Color(220, 252, 231));
            statusBadge.setForeground(new Color(22, 101, 52));
        } else {
            statusBadge.setBackground(new Color(254, 226, 226));
            statusBadge.setForeground(new Color(153, 27, 27));
        }

        header.add(statusBadge, BorderLayout.EAST);
        return header;
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 12, 0));
        panel.setOpaque(false);

        panel.add(createMetricCard("FINAL SCORE", detail.getScore() + " / " + detail.getTotalQuestions(), new Color(79, 70, 229)));

        // Percentage Card with JProgressBar
        JPanel pctCard = createMetricCardWithBar("PERCENTAGE", String.format("%.1f%%", detail.getPercentage()),
                (int) Math.round(detail.getPercentage()), new Color(37, 99, 235));
        panel.add(pctCard);

        panel.add(createMetricCard("CORRECT ANSWERS", String.valueOf(detail.getCorrectAnswers()), new Color(22, 163, 74)));
        panel.add(createMetricCard("INCORRECT ANSWERS", String.valueOf(detail.getIncorrectAnswers()), new Color(220, 38, 38)));
        panel.add(createMetricCard("UNANSWERED", String.valueOf(detail.getUnansweredQuestions()), new Color(217, 119, 6)));

        return panel;
    }

    private JPanel createMetricCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(new Color(15, 23, 42));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricCardWithBar(String title, String value, int percentVal, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valLbl.setForeground(new Color(15, 23, 42));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percentVal);
        bar.setForeground(accent);
        bar.setBackground(new Color(241, 245, 249));
        bar.setPreferredSize(new Dimension(100, 6));
        bar.setBorderPainted(false);

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(valLbl, BorderLayout.NORTH);
        content.add(bar, BorderLayout.SOUTH);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTableAndExplanationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Questions JTable
        String[] columns = {"#", "Question", "Your Answer", "Correct Answer", "Result"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (QuestionResultDetail q : detail.getQuestionDetails()) {
            String yourAns = q.getSelectedOptionLabel() != null && !"-".equals(q.getSelectedOptionLabel()) ?
                    (q.getSelectedOptionLabel() + ". " + q.getSelectedOptionText()) : "Unanswered";
            String correctAns = q.getCorrectOptionLabel() + ". " + q.getCorrectOptionText();

            tableModel.addRow(new Object[]{
                    "Q" + q.getQuestionOrder(),
                    q.getQuestionText(),
                    yourAns,
                    correctAns,
                    q.getResult()
            });
        }

        questionTable = new JTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(34);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionTable.setGridColor(new Color(241, 245, 249));
        questionTable.setShowHorizontalLines(true);
        questionTable.setShowVerticalLines(false);

        JTableHeader header = questionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        questionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(180);

        // Result column renderer
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected) {
                    String res = value != null ? value.toString() : "";
                    switch (res) {
                        case "CORRECT" -> {
                            lbl.setForeground(new Color(22, 101, 52));
                            lbl.setBackground(new Color(220, 252, 231));
                        }
                        case "INCORRECT" -> {
                            lbl.setForeground(new Color(153, 27, 27));
                            lbl.setBackground(new Color(254, 226, 226));
                        }
                        default -> {
                            lbl.setForeground(new Color(180, 83, 9));
                            lbl.setBackground(new Color(254, 243, 199));
                        }
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        JScrollPane scrollTable = new JScrollPane(questionTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollTable.getViewport().setBackground(Color.WHITE);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Explanation Box below table
        JPanel explanationPanel = new JPanel(new BorderLayout(0, 4));
        explanationPanel.setOpaque(false);
        explanationPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JLabel expHeader = new JLabel("Question Explanation (Select a question above to inspect):");
        expHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        expHeader.setForeground(new Color(71, 85, 105));
        explanationPanel.add(expHeader, BorderLayout.NORTH);

        explanationArea = new JTextArea(3, 40);
        explanationArea.setEditable(false);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setText("Select any row above to view the explanation provided by the instructor.");
        explanationArea.setBackground(new Color(241, 245, 249));
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JScrollPane expScroll = new JScrollPane(explanationArea);
        expScroll.setBorder(BorderFactory.createEmptyBorder());
        explanationPanel.add(expScroll, BorderLayout.CENTER);

        panel.add(explanationPanel, BorderLayout.SOUTH);

        // Selection listener to update explanation
        questionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = questionTable.getSelectedRow();
                if (row >= 0 && row < detail.getQuestionDetails().size()) {
                    QuestionResultDetail q = detail.getQuestionDetails().get(row);
                    String exp = q.getExplanation();
                    if (exp != null && !exp.trim().isEmpty()) {
                        explanationArea.setText(exp);
                    } else {
                        explanationArea.setText("No additional explanation was provided for this question.");
                    }
                }
            }
        });

        return panel;
    }
}
