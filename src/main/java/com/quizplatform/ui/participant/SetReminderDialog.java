package com.quizplatform.ui.participant;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizReminder;
import com.quizplatform.model.User;
import com.quizplatform.service.QuizReminderService;
import com.quizplatform.service.impl.QuizReminderServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * Dialog allowing participants to schedule an upcoming quiz reminder.
 */
public class SetReminderDialog extends JDialog {

    private final User participant;
    private final Quiz quiz;
    private final QuizReminderService reminderService;
    private final Runnable onSuccess;

    private JSpinner dateSpinner;
    private SpinnerDateModel dateModel;

    public SetReminderDialog(Frame parent, User participant, Quiz quiz, Runnable onSuccess) {
        super(parent, "Set Quiz Reminder", true);
        this.participant = participant;
        this.quiz = quiz;
        this.reminderService = new QuizReminderServiceImpl();
        this.onSuccess = onSuccess;

        initUI();
    }

    private void initUI() {
        setSize(460, 420);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel titleLbl = new JLabel("⏰ Set Quiz Reminder");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // 2. Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(248, 250, 252));
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Quiz info card
        JPanel quizCard = new JPanel(new BorderLayout());
        quizCard.setBackground(Color.WHITE);
        quizCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel quizNameLbl = new JLabel(quiz.getTitle());
        quizNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quizNameLbl.setForeground(new Color(15, 23, 42));

        JLabel quizDurationLbl = new JLabel("Duration: " + quiz.getDurationMinutes() + " minutes");
        quizDurationLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quizDurationLbl.setForeground(new Color(100, 116, 139));

        quizCard.add(quizNameLbl, BorderLayout.NORTH);
        quizCard.add(Box.createRigidArea(new Dimension(0, 4)), BorderLayout.CENTER);
        quizCard.add(quizDurationLbl, BorderLayout.SOUTH);
        content.add(quizCard);

        content.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel lblPresets = new JLabel("Quick Presets:");
        lblPresets.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPresets.setForeground(new Color(51, 65, 85));
        content.add(lblPresets);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel presetsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        presetsGrid.setOpaque(false);

        JButton btn15m = createPresetButton("+15 Minutes", 15);
        JButton btn30m = createPresetButton("+30 Minutes", 30);
        JButton btn1h = createPresetButton("+1 Hour", 60);
        JButton btnTomorrow = new JButton("Tomorrow 9:00 AM");
        stylePresetButton(btnTomorrow);
        btnTomorrow.addActionListener(e -> {
            LocalDateTime tomorrowMorning = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(9, 0));
            setSpinnerTime(tomorrowMorning);
        });

        presetsGrid.add(btn15m);
        presetsGrid.add(btn30m);
        presetsGrid.add(btn1h);
        presetsGrid.add(btnTomorrow);
        content.add(presetsGrid);

        content.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel lblCustom = new JLabel("Custom Reminder Date & Time:");
        lblCustom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCustom.setForeground(new Color(51, 65, 85));
        content.add(lblCustom);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // Date spinner default: 30 minutes from now
        Date defaultDate = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
        dateModel = new SpinnerDateModel(defaultDate, null, null, java.util.Calendar.MINUTE);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateSpinner.setPreferredSize(new Dimension(0, 34));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm"));
        content.add(dateSpinner);

        add(content, BorderLayout.CENTER);

        // 3. Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(90, 34));
        btnCancel.setBackground(new Color(241, 245, 249));
        btnCancel.setForeground(new Color(51, 65, 85));
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);

        JButton btnSchedule = new JButton("Schedule Reminder");
        btnSchedule.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSchedule.setPreferredSize(new Dimension(160, 34));
        btnSchedule.setBackground(new Color(37, 99, 235));
        btnSchedule.setForeground(Color.WHITE);
        btnSchedule.setBorder(BorderFactory.createEmptyBorder());
        btnSchedule.setFocusPainted(false);
        btnSchedule.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSchedule.addActionListener(e -> handleSchedule());
        footer.add(btnSchedule);

        add(footer, BorderLayout.SOUTH);
    }

    private JButton createPresetButton(String text, int minutesToAdd) {
        JButton btn = new JButton(text);
        stylePresetButton(btn);
        btn.addActionListener(e -> setSpinnerTime(LocalDateTime.now().plusMinutes(minutesToAdd)));
        return btn;
    }

    private void stylePresetButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 58, 138));
        btn.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setSpinnerTime(LocalDateTime time) {
        dateModel.setValue(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void handleSchedule() {
        Date selectedDate = (Date) dateSpinner.getValue();
        LocalDateTime reminderTime = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (reminderTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "Reminder time must be set in the future.",
                    "Invalid Time",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            reminderService.scheduleReminder(participant.getId(), quiz.getId(), reminderTime);
            JOptionPane.showMessageDialog(this,
                    "Reminder scheduled successfully for " + quiz.getTitle() + "!",
                    "Reminder Set",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Scheduling Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
