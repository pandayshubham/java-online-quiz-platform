package com.quizplatform.ui.creator;

import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.User;
import com.quizplatform.service.QuestionService;
import com.quizplatform.service.QuizService;
import com.quizplatform.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Panel for managing questions and their options for a selected quiz.
 */
public class QuestionManagementPanel extends JPanel {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final Runnable onDataChanged;

    private JComboBox<Quiz> quizComboBox;
    private JLabel quizInfoBadge;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private List<Question> currentQuestions = new ArrayList<>();

    public QuestionManagementPanel(QuizService quizService, QuestionService questionService, Runnable onDataChanged) {
        this.quizService = quizService;
        this.questionService = questionService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initTopBar();
        initTable();
        initBottomBar();

        reloadQuizzesList();
    }

    private void initTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        // Header Title
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Question Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Add, edit, and organize multiple choice questions with A/B/C/D options.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(titleLabel);
        textPanel.add(subLabel);
        topPanel.add(textPanel, BorderLayout.WEST);

        // Quiz Selector
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        selectorPanel.setOpaque(false);

        JLabel selectLabel = new JLabel("Select Quiz:");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectorPanel.add(selectLabel);

        quizComboBox = new JComboBox<>();
        quizComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quizComboBox.setPreferredSize(new Dimension(280, 34));
        quizComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Quiz q) {
                    setText("#" + q.getId() + " - " + q.getTitle() + " (" + q.getStatus() + ")");
                } else if (value == null) {
                    setText("-- No Quizzes Available --");
                }
                return this;
            }
        });
        quizComboBox.addActionListener(e -> loadQuestionsForSelectedQuiz());
        selectorPanel.add(quizComboBox);

        quizInfoBadge = new JLabel("0 Questions");
        quizInfoBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quizInfoBadge.setForeground(new Color(79, 70, 229));
        quizInfoBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 231, 255)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        quizInfoBadge.setOpaque(true);
        quizInfoBadge.setBackground(new Color(238, 242, 255));
        selectorPanel.add(quizInfoBadge);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            reloadQuizzesList();
            loadQuestionsForSelectedQuiz();
        });
        selectorPanel.add(refreshBtn);

        topPanel.add(selectorPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"#", "Question Text", "Option A", "Option B", "Option C", "Option D", "Correct", "Explanation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(38);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        questionTable.setGridColor(new Color(241, 245, 249));
        questionTable.setShowHorizontalLines(true);
        questionTable.setShowVerticalLines(false);

        // Header
        JTableHeader header = questionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Column widths
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        questionTable.getColumnModel().getColumn(7).setPreferredWidth(160);

        // Center align order and correct answer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        questionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Highlight correct answer column
        questionTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!isSelected) {
                    lbl.setForeground(new Color(22, 101, 52));
                    lbl.setBackground(new Color(220, 252, 231));
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initBottomBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton addBtn = new JButton("+ Add Question");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setBackground(new Color(124, 58, 237));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> handleAddQuestion());

        JButton editBtn = new JButton("Edit Question");
        editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editBtn.setBackground(new Color(241, 245, 249));
        editBtn.setFocusPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> handleEditQuestion());

        JButton deleteBtn = new JButton("Delete Question");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setBackground(new Color(254, 226, 226));
        deleteBtn.setForeground(new Color(185, 28, 28));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> handleDeleteQuestion());

        bottomPanel.add(addBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(deleteBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void reloadQuizzesList() {
        User creator = SessionManager.getCurrentUser();
        if (creator == null) return;

        Quiz previouslySelected = (Quiz) quizComboBox.getSelectedItem();
        quizComboBox.removeAllItems();

        List<Quiz> quizzes = quizService.getQuizzesByCreator(creator.getId());
        for (Quiz q : quizzes) {
            quizComboBox.addItem(q);
        }

        if (previouslySelected != null) {
            for (int i = 0; i < quizComboBox.getItemCount(); i++) {
                if (quizComboBox.getItemAt(i).getId() == previouslySelected.getId()) {
                    quizComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void setSelectedQuiz(Quiz targetQuiz) {
        if (targetQuiz == null) return;
        reloadQuizzesList();
        for (int i = 0; i < quizComboBox.getItemCount(); i++) {
            Quiz item = quizComboBox.getItemAt(i);
            if (item != null && item.getId() == targetQuiz.getId()) {
                quizComboBox.setSelectedIndex(i);
                break;
            }
        }
        loadQuestionsForSelectedQuiz();
    }

    public void loadQuestionsForSelectedQuiz() {
        tableModel.setRowCount(0);
        Quiz selectedQuiz = (Quiz) quizComboBox.getSelectedItem();

        if (selectedQuiz == null) {
            quizInfoBadge.setText("No Quiz Selected");
            currentQuestions.clear();
            return;
        }

        try {
            currentQuestions = questionService.getQuestionsByQuizId(selectedQuiz.getId());
            quizInfoBadge.setText(currentQuestions.size() + " Questions | Status: " + selectedQuiz.getStatus());

            for (Question q : currentQuestions) {
                List<Option> options = questionService.getOptionsByQuestionId(q.getId());
                String optA = "", optB = "", optC = "", optD = "";
                String correct = "-";

                for (Option opt : options) {
                    if ("A".equalsIgnoreCase(opt.getOptionLabel())) {
                        optA = opt.getOptionText();
                        if (opt.isCorrect()) correct = "A";
                    } else if ("B".equalsIgnoreCase(opt.getOptionLabel())) {
                        optB = opt.getOptionText();
                        if (opt.isCorrect()) correct = "B";
                    } else if ("C".equalsIgnoreCase(opt.getOptionLabel())) {
                        optC = opt.getOptionText();
                        if (opt.isCorrect()) correct = "C";
                    } else if ("D".equalsIgnoreCase(opt.getOptionLabel())) {
                        optD = opt.getOptionText();
                        if (opt.isCorrect()) correct = "D";
                    }
                }

                tableModel.addRow(new Object[]{
                        q.getQuestionOrder(),
                        q.getQuestionText(),
                        optA,
                        optB,
                        optC,
                        optD,
                        correct,
                        q.getExplanation() != null ? q.getExplanation() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Question getSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row < 0 || row >= currentQuestions.size()) {
            JOptionPane.showMessageDialog(this, "Please select a question from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentQuestions.get(row);
    }

    private void handleAddQuestion() {
        Quiz selectedQuiz = (Quiz) quizComboBox.getSelectedItem();
        if (selectedQuiz == null) {
            JOptionPane.showMessageDialog(this, "Please create or select a quiz first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        QuestionFormDialog dialog = new QuestionFormDialog(parent, questionService, selectedQuiz.getId(), null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadQuestionsForSelectedQuiz();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void handleEditQuestion() {
        Quiz selectedQuiz = (Quiz) quizComboBox.getSelectedItem();
        if (selectedQuiz == null) return;

        Question selectedQuestion = getSelectedQuestion();
        if (selectedQuestion == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        QuestionFormDialog dialog = new QuestionFormDialog(parent, questionService, selectedQuiz.getId(), selectedQuestion);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadQuestionsForSelectedQuiz();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void handleDeleteQuestion() {
        Question selectedQuestion = getSelectedQuestion();
        if (selectedQuestion == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete question #" + selectedQuestion.getQuestionOrder() + "?\n\"" + selectedQuestion.getQuestionText() + "\"",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = questionService.deleteQuestion(selectedQuestion.getId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Question deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadQuestionsForSelectedQuiz();
                    if (onDataChanged != null) onDataChanged.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to delete question.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting question: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
