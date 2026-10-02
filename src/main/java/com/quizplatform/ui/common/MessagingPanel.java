package com.quizplatform.ui.common;

import com.quizplatform.model.Message;
import com.quizplatform.model.User;
import com.quizplatform.service.MessageService;
import com.quizplatform.service.impl.MessageServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Modern Messaging Panel enabling direct communication between participants
 * and quiz creators (or administrators) with conversation history, contact lists,
 * unread badges, and chat bubble layout.
 */
public class MessagingPanel extends JPanel {

    private final User currentUser;
    private final MessageService messageService;

    // Contacts
    private DefaultListModel<User> contactListModel;
    private JList<User> contactList;
    private JTextField searchContactField;
    private List<User> allContacts = new ArrayList<>();
    private User activePartner = null;

    // Chat Window
    private JLabel lblChatHeader;
    private JLabel lblChatSubheader;
    private JPanel chatHistoryContainer;
    private JScrollPane chatScrollPane;
    private JTextArea inputArea;
    private JButton btnSend;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    public MessagingPanel(User currentUser) {
        this(currentUser, new MessageServiceImpl());
    }

    public MessagingPanel(User currentUser, MessageService messageService) {
        this.currentUser = currentUser;
        this.messageService = messageService;

        initUI();
        loadContacts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(4);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Left: Contacts Sidebar
        splitPane.setLeftComponent(createContactsPanel());

        // Right: Chat Conversation Area
        splitPane.setRightComponent(createChatAreaPanel());

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createContactsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240)));

        // Header & Search
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("💬 Messages");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerRow.add(lblTitle, BorderLayout.WEST);

        JButton btnRefreshContacts = new JButton("↻");
        btnRefreshContacts.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefreshContacts.setToolTipText("Refresh Contacts");
        btnRefreshContacts.setPreferredSize(new Dimension(30, 26));
        btnRefreshContacts.setBackground(new Color(241, 245, 249));
        btnRefreshContacts.setBorder(BorderFactory.createEmptyBorder());
        btnRefreshContacts.setFocusPainted(false);
        btnRefreshContacts.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefreshContacts.addActionListener(e -> loadContacts());
        headerRow.add(btnRefreshContacts, BorderLayout.EAST);
        top.add(headerRow, BorderLayout.NORTH);

        searchContactField = new JTextField();
        searchContactField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchContactField.setPreferredSize(new Dimension(0, 32));
        searchContactField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        searchContactField.putClientProperty("JTextField.placeholderText", "Search contacts...");
        searchContactField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterContacts(searchContactField.getText());
            }
        });
        top.add(searchContactField, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        // Contacts list
        contactListModel = new DefaultListModel<>();
        contactList = new JList<>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel card = new JPanel(new BorderLayout(8, 0));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));

                if (isSelected) {
                    card.setBackground(new Color(239, 246, 255));
                } else {
                    card.setBackground(Color.WHITE);
                }

                if (value instanceof User u) {
                    JPanel infoPanel = new JPanel();
                    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                    infoPanel.setOpaque(false);

                    JLabel nameLbl = new JLabel(u.getName());
                    nameLbl.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 13));
                    nameLbl.setForeground(new Color(15, 23, 42));

                    String roleTag = u.getRole() != null ? "[" + u.getRole().name().replace('_', ' ') + "]" : "";
                    JLabel roleLbl = new JLabel(roleTag + " • " + u.getEmail());
                    roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    roleLbl.setForeground(new Color(100, 116, 139));

                    infoPanel.add(nameLbl);
                    infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
                    infoPanel.add(roleLbl);
                    card.add(infoPanel, BorderLayout.CENTER);

                    // Unread badge if recipient has unread messages from this user
                    int unread = messageService.getConversation(currentUser.getId(), u.getId()).stream()
                            .mapToInt(m -> (m.getReceiverId() == currentUser.getId() && !m.isRead()) ? 1 : 0)
                            .sum();

                    if (unread > 0) {
                        JLabel unreadBadge = new JLabel(" " + unread + " ");
                        unreadBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        unreadBadge.setOpaque(true);
                        unreadBadge.setBackground(new Color(59, 130, 246));
                        unreadBadge.setForeground(Color.WHITE);
                        unreadBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                        card.add(unreadBadge, BorderLayout.EAST);
                    }
                }
                return card;
            }
        });

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selected = contactList.getSelectedValue();
                if (selected != null) {
                    activePartner = selected;
                    openConversation(selected);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(contactList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatAreaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 250, 252));

        // 1. Chat Header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(Color.WHITE);
        chatHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JPanel headerInfo = new JPanel();
        headerInfo.setLayout(new BoxLayout(headerInfo, BoxLayout.Y_AXIS));
        headerInfo.setOpaque(false);

        lblChatHeader = new JLabel("Select a contact to begin messaging");
        lblChatHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChatHeader.setForeground(new Color(15, 23, 42));

        lblChatSubheader = new JLabel("Your conversations will be saved and synced.");
        lblChatSubheader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChatSubheader.setForeground(new Color(100, 116, 139));

        headerInfo.add(lblChatHeader);
        headerInfo.add(Box.createRigidArea(new Dimension(0, 2)));
        headerInfo.add(lblChatSubheader);
        chatHeader.add(headerInfo, BorderLayout.WEST);

        panel.add(chatHeader, BorderLayout.NORTH);

        // 2. Chat history scroll pane
        chatHistoryContainer = new JPanel();
        chatHistoryContainer.setLayout(new BoxLayout(chatHistoryContainer, BoxLayout.Y_AXIS));
        chatHistoryContainer.setBackground(new Color(248, 250, 252));
        chatHistoryContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        chatScrollPane = new JScrollPane(chatHistoryContainer);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.getViewport().setBackground(new Color(248, 250, 252));
        panel.add(chatScrollPane, BorderLayout.CENTER);

        // 3. Message input area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        inputArea = new JTextArea(2, 30);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setEnabled(false);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    handleSendMessage();
                }
            }
        });

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        btnSend = new JButton("Send");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setPreferredSize(new Dimension(85, 42));
        btnSend.setBackground(new Color(37, 99, 235));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setBorder(BorderFactory.createEmptyBorder());
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.setEnabled(false);
        btnSend.addActionListener(e -> handleSendMessage());
        inputPanel.add(btnSend, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void loadContacts() {
        allContacts = messageService.getAvailableContacts(currentUser);
        filterContacts(searchContactField != null ? searchContactField.getText() : "");
    }

    private void filterContacts(String query) {
        contactListModel.clear();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (User u : allContacts) {
            if (q.isEmpty() || u.getName().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)) {
                contactListModel.addElement(u);
            }
        }

        if (activePartner != null) {
            contactList.setSelectedValue(activePartner, true);
        }
    }

    private void openConversation(User partner) {
        lblChatHeader.setText(partner.getName());
        lblChatSubheader.setText(partner.getRole().name().replace('_', ' ') + " • " + partner.getEmail());
        inputArea.setEnabled(true);
        btnSend.setEnabled(true);
        inputArea.requestFocusInWindow();

        // Mark conversation as read
        messageService.markConversationAsRead(currentUser.getId(), partner.getId());

        renderConversation(partner);
        contactList.repaint();
    }

    private void renderConversation(User partner) {
        chatHistoryContainer.removeAll();

        List<Message> conversation = messageService.getConversation(currentUser.getId(), partner.getId());

        if (conversation.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setBorder(BorderFactory.createEmptyBorder(60, 20, 60, 20));
            JLabel lbl = new JLabel("No messages yet. Send a message to start the conversation!");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(148, 163, 184));
            empty.add(lbl);
            chatHistoryContainer.add(empty);
        } else {
            for (Message msg : conversation) {
                boolean isSentByMe = (msg.getSenderId() == currentUser.getId());
                chatHistoryContainer.add(createMessageBubble(msg, isSentByMe));
                chatHistoryContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        chatHistoryContainer.revalidate();
        chatHistoryContainer.repaint();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            var verticalBar = chatScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private JPanel createMessageBubble(Message msg, boolean isSentByMe) {
        JPanel row = new JPanel(new FlowLayout(isSentByMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));

        if (isSentByMe) {
            bubble.setBackground(new Color(37, 99, 235)); // Primary blue
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(29, 78, 216), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        } else {
            bubble.setBackground(Color.WHITE);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }

        JLabel textLbl = new JLabel("<html><body style='width: 320px;'>" + msg.getMessageText() + "</body></html>");
        textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLbl.setForeground(isSentByMe ? Color.WHITE : new Color(15, 23, 42));
        bubble.add(textLbl);

        bubble.add(Box.createRigidArea(new Dimension(0, 4)));

        String timeStr = msg.getCreatedAt() != null ? msg.getCreatedAt().format(DATE_FORMATTER) : "";
        String statusStr = isSentByMe ? (msg.isRead() ? " ✓✓ Read" : " ✓ Sent") : "";
        JLabel metaLbl = new JLabel(timeStr + statusStr);
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        metaLbl.setForeground(isSentByMe ? new Color(191, 219, 254) : new Color(148, 163, 184));
        metaLbl.setAlignmentX(isSentByMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubble.add(metaLbl);

        row.add(bubble);
        return row;
    }

    private void handleSendMessage() {
        if (activePartner == null) return;
        String text = inputArea.getText();
        if (text == null || text.trim().isEmpty()) return;

        int msgId = messageService.sendMessage(currentUser.getId(), activePartner.getId(), text.trim());
        if (msgId > 0) {
            inputArea.setText("");
            renderConversation(activePartner);
            contactList.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
