package com.tuca;

import com.tuca.connection.DatabaseManager;
import com.tuca.manager.TaskManager;
import com.tuca.model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    private final DatabaseManager dbManager;
    private final TaskManager taskManager;
    private final JFrame frame;
    private final JPanel taskPanel;
    private final JScrollPane scrollPane;

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private String currentStatusFilter = "TODAS";
    private String currentSearchText = "";

    private static final Color PRIMARY_DARK = new Color(30, 41, 59);
    private static final Color PRIMARY = new Color(51, 65, 85);
    private static final Color ACCENT = new Color(34, 197, 94);
    private static final Color ACCENT_HOVER = new Color(22, 163, 74);
    private static final Color BACKGROUND = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SECONDARY = new Color(59, 130, 246);
    private static final Color SECONDARY_HOVER = new Color(37, 99, 235);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color DANGER_HOVER = new Color(220, 38, 38);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    public Main() {
        this.dbManager = new DatabaseManager();
        dbManager.startConnection();
        this.taskManager = new TaskManager(dbManager);

        this.frame = new JFrame("Task Manager");
        this.taskPanel = createTasksContainerPanel();
        this.scrollPane = createTasksScrollPane();

        setupMainFrame();
        buildUI();
        refreshTasks();
        startAutoUpdateScheduler();
    }

    private void setupMainFrame() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(BACKGROUND);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleApplicationClose();
            }
        });
    }

    private void handleApplicationClose() {
        System.out.println("[Tasks] Atualizando todas as tarefas antes de fechar...");
        refreshTasks();
        dbManager.closeConnection();
        frame.dispose();
        System.out.println("[Tasks] Aplicação encerrada com segurança.");
        System.exit(0);
    }

    private void buildUI() {
        frame.add(createModernHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));

        mainContent.add(createModernFilterPanel(), BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainContent, BorderLayout.CENTER);
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createModernHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSection.setBackground(PRIMARY_DARK);

        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Organize suas tarefas com eficiência");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(148, 163, 184));

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(PRIMARY_DARK);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(5));
        titleContainer.add(subtitleLabel);

        leftSection.add(titleContainer);
        headerPanel.add(leftSection, BorderLayout.WEST);

        JPanel buttonContainer = createHeaderButtonPanel();
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createHeaderButtonPanel() {
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonContainer.setBackground(PRIMARY_DARK);

        JButton updateButton = createModernButton("Atualizar", SECONDARY, SECONDARY_HOVER, e -> handleUpdateTasks());
        updateButton.setPreferredSize(new Dimension(130, 44));

        JButton createButton = createModernButton("+ Nova Tarefa", ACCENT, ACCENT_HOVER, e -> createNewTask());
        createButton.setPreferredSize(new Dimension(160, 44));

        buttonContainer.add(updateButton);
        buttonContainer.add(createButton);

        return buttonContainer;
    }

    private JPanel createModernFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout(20, 0));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JPanel leftPanel = createModernSearchPanel();
        JPanel rightPanel = createModernStatusFilterPanel();

        filterPanel.add(leftPanel, BorderLayout.WEST);
        filterPanel.add(rightPanel, BorderLayout.EAST);

        return filterPanel;
    }

    private JPanel createModernSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchPanel.setBackground(CARD_BG);

        JLabel searchLabel = new JLabel("Pesquisar");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(TEXT_PRIMARY);

        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setBackground(BACKGROUND);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        searchField.setText("Buscar por descrição, responsável ou ID...");
        searchField.setForeground(TEXT_SECONDARY);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Buscar por descrição, responsável ou ID...")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Buscar por descrição, responsável ou ID...");
                    searchField.setForeground(TEXT_SECONDARY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        return searchPanel;
    }

    private JPanel createModernStatusFilterPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusPanel.setBackground(CARD_BG);

        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(TEXT_PRIMARY);

        String[] statusOptions = {"TODAS", "PENDENTE", "COMPLETA", "INCOMPLETA", "ATRASADA"};
        statusFilterCombo = new JComboBox<>(statusOptions);
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilterCombo.setPreferredSize(new Dimension(160, 44));
        statusFilterCombo.setBackground(BACKGROUND);
        statusFilterCombo.setForeground(TEXT_PRIMARY);
        statusFilterCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        statusFilterCombo.addActionListener(e -> {
            currentStatusFilter = (String) statusFilterCombo.getSelectedItem();
            applyFilters();
        });

        JButton clearButton = createModernButton("Limpar Filtros", new Color(148, 163, 184), new Color(100, 116, 139), e -> clearAllFilters());
        clearButton.setPreferredSize(new Dimension(130, 44));

        statusPanel.add(statusLabel);
        statusPanel.add(statusFilterCombo);
        statusPanel.add(clearButton);

        return statusPanel;
    }

    private void clearAllFilters() {
        searchField.setText("Buscar por descrição, responsável ou ID...");
        searchField.setForeground(TEXT_SECONDARY);
        statusFilterCombo.setSelectedIndex(0);
        currentStatusFilter = "TODAS";
        currentSearchText = "";
        applyFilters();
    }

    private void applyFilters() {
        String text = searchField.getText().trim();
        if (!text.equals("Buscar por descrição, responsável ou ID...")) {
            currentSearchText = text.toLowerCase();
        } else {
            currentSearchText = "";
        }
        refreshTasks();
    }

    private JPanel createTasksContainerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);
        return panel;
    }

    private JScrollPane createTasksScrollPane() {
        JScrollPane sp = new JScrollPane(taskPanel);
        sp.setBorder(null);
        sp.setBackground(BACKGROUND);
        sp.getViewport().setBackground(BACKGROUND);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void handleUpdateTasks() {
        taskManager.updateTasks();
        showModernDialog("Tarefas atualizadas com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        refreshTasks();
    }

    private void startAutoUpdateScheduler() {
        System.out.println("[Tasks] Iniciando agendador de atualização automática...");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshTasks, 0, 1, TimeUnit.MINUTES);
    }

    private void createNewTask() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField descField = createModernTextField();
        JTextField ownerField = createModernTextField();
        JTextField daysField = createModernTextField();
        daysField.setText("7");

        panel.add(new JLabel("Descrição:"));
        panel.add(descField);
        panel.add(new JLabel("Responsável:"));
        panel.add(ownerField);
        panel.add(new JLabel("Prazo (dias):"));
        panel.add(daysField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Criar Nova Tarefa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            handleTaskCreation(descField.getText(), ownerField.getText(), daysField.getText());
        }
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private void handleTaskCreation(String description, String owner, String daysStr) {
        description = description.trim();
        owner = owner.trim();
        daysStr = daysStr.trim();

        if (description.isEmpty() || owner.isEmpty()) {
            showModernDialog("Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int days = Integer.parseInt(daysStr);
            taskManager.createTask(description, owner, days);
            showModernDialog("Tarefa criada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            refreshTasks();
        } catch (NumberFormatException ex) {
            showModernDialog("Prazo deve ser um número inteiro!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshTasks() {
        SwingUtilities.invokeLater(() -> {
            taskPanel.removeAll();
            List<Task> tasks = taskManager.loadAllTasks();

            List<Task> filteredTasks = filterTasks(tasks);

            if (filteredTasks.isEmpty()) {
                displayModernEmptyState();
            } else {
                displayTasks(filteredTasks);
            }

            taskPanel.revalidate();
            taskPanel.repaint();
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    private List<Task> filterTasks(List<Task> tasks) {
        return tasks.stream()
                .filter(this::matchesStatusFilter)
                .filter(this::matchesSearchFilter)
                .collect(Collectors.toList());
    }

    private boolean matchesStatusFilter(Task task) {
        return currentStatusFilter.equals("TODAS") ||
                task.getStatus().trim().equalsIgnoreCase(currentStatusFilter);
    }

    private boolean matchesSearchFilter(Task task) {
        if (currentSearchText.isEmpty()) return true;

        return task.getDescription().toLowerCase().contains(currentSearchText) ||
                task.getOwnerName().toLowerCase().contains(currentSearchText) ||
                String.valueOf(task.getId()).contains(currentSearchText);
    }

    private void displayTasks(List<Task> tasks) {
        for (Task task : tasks) {
            taskPanel.add(createModernTaskCard(task));
            taskPanel.add(Box.createVerticalStrut(16));
        }
    }

    private void displayModernEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBackground(BACKGROUND);
        emptyPanel.setBorder(new EmptyBorder(60, 0, 60, 0));

        JLabel emptyIcon = new JLabel("✓");
        emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        emptyIcon.setForeground(TEXT_SECONDARY);
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyTitle = new JLabel("Nenhuma tarefa encontrada");
        emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        emptyTitle.setForeground(TEXT_PRIMARY);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyDesc = new JLabel("Tente ajustar os filtros ou criar uma nova tarefa");
        emptyDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyDesc.setForeground(TEXT_SECONDARY);
        emptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(emptyIcon);
        emptyPanel.add(Box.createVerticalStrut(16));
        emptyPanel.add(emptyTitle);
        emptyPanel.add(Box.createVerticalStrut(8));
        emptyPanel.add(emptyDesc);

        taskPanel.add(emptyPanel);
    }

    private JPanel createModernTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel statusIndicator = new JPanel();
        statusIndicator.setPreferredSize(new Dimension(5, 0));
        statusIndicator.setBackground(getStatusColor(task.getStatus()));
        card.add(statusIndicator, BorderLayout.WEST);

        card.add(createModernTaskInfo(task), BorderLayout.CENTER);
        card.add(createModernActionButtons(task), BorderLayout.EAST);

        return card;
    }

    private JPanel createModernTaskInfo(Task task) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_BG);

        JLabel titleLabel = new JLabel(task.getDescription());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        metaPanel.setBackground(CARD_BG);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaPanel.add(createMetaBadge("ID: " + task.getId(), new Color(241, 245, 249)));
        metaPanel.add(Box.createHorizontalStrut(8));
        metaPanel.add(createMetaBadge(task.getOwnerName(), new Color(219, 234, 254)));
        metaPanel.add(Box.createHorizontalStrut(8));
        metaPanel.add(createStatusBadge(task.getStatus()));

        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        datesPanel.setBackground(CARD_BG);
        datesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel startDate = new JLabel("Início: " + formatDate(task.getStartDate()));
        startDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startDate.setForeground(TEXT_SECONDARY);

        JLabel separator = new JLabel(" • ");
        separator.setForeground(TEXT_SECONDARY);

        JLabel endDate = new JLabel("Prazo: " + formatDate(task.getExpiryDate()));
        endDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        endDate.setForeground(TEXT_SECONDARY);

        datesPanel.add(startDate);
        datesPanel.add(separator);
        datesPanel.add(endDate);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(metaPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(datesPanel);

        return infoPanel;
    }

    private JLabel createMetaBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(TEXT_PRIMARY);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        return badge;
    }

    private JLabel createStatusBadge(String status) {
        Color statusColor = getStatusColor(status);
        Color bgColor = new Color(
                statusColor.getRed(),
                statusColor.getGreen(),
                statusColor.getBlue(),
                30
        );

        JLabel badge = new JLabel(status.toUpperCase());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(statusColor);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        return badge;
    }

    private JPanel createModernActionButtons(Task task) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(CARD_BG);

        JButton editBtn = createModernButton("Editar", SECONDARY, SECONDARY_HOVER, e -> handleEditDescription(task));
        editBtn.setPreferredSize(new Dimension(120, 40));
        editBtn.setMaximumSize(new Dimension(120, 40));
        editBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton statusBtn = createModernButton("Status", new Color(139, 92, 246), new Color(124, 58, 237), e -> handleEditStatus(task));
        statusBtn.setPreferredSize(new Dimension(120, 40));
        statusBtn.setMaximumSize(new Dimension(120, 40));
        statusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton completeBtn = createModernButton("Completar", ACCENT, ACCENT_HOVER, e -> handleCompleteTask(task));
        completeBtn.setPreferredSize(new Dimension(120, 40));
        completeBtn.setMaximumSize(new Dimension(120, 40));
        completeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton deleteBtn = createModernButton("Deletar", DANGER, DANGER_HOVER, e -> handleDeleteTask(task));
        deleteBtn.setPreferredSize(new Dimension(120, 40));
        deleteBtn.setMaximumSize(new Dimension(120, 40));
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(editBtn);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(statusBtn);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(completeBtn);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(deleteBtn);

        return buttonPanel;
    }

    private void handleEditDescription(Task task) {
        String newDescription = (String) JOptionPane.showInputDialog(
                frame,
                "Nova descrição:",
                "Editar Tarefa",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                task.getDescription()
        );

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            taskManager.updateTaskDescription(task.getId(), newDescription.trim());
            showModernDialog("Descrição atualizada!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            refreshTasks();
        }
    }

    private void handleEditStatus(Task task) {
        String[] options = {"Pendente", "Completa", "Incompleta", "Atrasada"};
        String newStatus = (String) JOptionPane.showInputDialog(
                frame,
                "Escolha o novo status:",
                "Alterar Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                task.getStatus()
        );

        if (newStatus != null) {
            taskManager.updateTaskStatus(task.getId(), newStatus);
            showModernDialog("Status atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            refreshTasks();
        }
    }

    private void handleCompleteTask(Task task) {
        int option = JOptionPane.showConfirmDialog(
                frame,
                "Deseja realmente completar esta tarefa?",
                "Confirmação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            taskManager.updateTaskStatus(task.getId(), "Completa");
            System.out.println("[Tasks] Task com ID: " + task.getId() + " foi completa.");
            refreshTasks();
        }
    }

    private void handleDeleteTask(Task task) {
        int option = JOptionPane.showConfirmDialog(
                frame,
                "Deseja realmente deletar esta tarefa? Esta ação não pode ser desfeita.",
                "Confirmação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            taskManager.deleteTask(task.getId());
            System.out.println("[Tasks] Task com ID: " + task.getId() + " foi deletada.");
            refreshTasks();
        }
    }

    private JButton createModernButton(String text, Color bgColor, Color hoverColor, ActionListener listener) {
        JButton button = new JButton(text) {
            private Color currentBg = bgColor;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            public void setCurrentBg(Color color) {
                this.currentBg = color;
                repaint();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ((JButton) evt.getSource()).setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                ((JButton) evt.getSource()).setBackground(bgColor);
            }
        });

        return button;
    }

    private Color getStatusColor(String status) {
        if (status == null) return TEXT_SECONDARY;

        return switch (status.trim().toUpperCase()) {
            case "COMPLETA" -> new Color(34, 197, 94);
            case "PENDENTE" -> new Color(234, 179, 8);
            case "INCOMPLETA" -> new Color(249, 115, 22);
            case "ATRASADA" -> new Color(239, 68, 68);
            default -> TEXT_SECONDARY;
        };
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    private void showModernDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.show();
        });
    }
}