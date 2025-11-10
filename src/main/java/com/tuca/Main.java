package com.tuca;

import com.tuca.model.*;
import com.tuca.service.ProducerService;
import com.tuca.service.TaskService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private final JFrame frame;
    private final JPanel taskPanel;
    private JPanel statusPanel;
    private final JScrollPane scrollPane;


    private final TaskService taskService;
    private final ProducerService producerService;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private int overdueCount = 0;
    private int completeCount = 0;
    private int pendingCount = 0;

    private static final String DEFAULT_SEARCH_STRING = "Buscar por descrição, responsável ou ID...";
    private static final String DEFAULT_FONT_NAME = "Segoe UI";
    private static final String DEFAULT_COMPLETE_STRING = "Completa";


    private String currentStatusFilter = "Todas";
    private String currentSearchText = "";

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final Color PRIMARY_DARK = new Color(30, 41, 59);
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

    @Autowired
    public Main(TaskService taskService, ProducerService producerService) {
        this.frame = new JFrame("Task Manager");
        this.taskPanel = createTasksContainerPanel();

        this.statusPanel = createStatusPanel();
        this.scrollPane = createTasksScrollPane();
        this.taskService = taskService;
        this.producerService = producerService;
        setupMainFrame();
        buildUI();
        refreshTasks();
    }

    private void setupMainFrame() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(BACKGROUND);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleApplicationClose();
            }
        });
    }

    @SneakyThrows
    private void handleApplicationClose() {

        producerService.sendEvent("CLOSING_PROGRAM");
        refreshTasks();
        frame.dispose();
        log.info("[Tasks] Application closed");
        System.exit(0);
    }

    private void buildUI() {
        frame.add(createModernHeader(), BorderLayout.NORTH);

        TPanel mainContent = new TPanel.Builder()
                .withBackground(BACKGROUND)
                .withBorderLayout(0, 20)
                .withBorder(20, 30, 20, 30)
                .build();

        mainContent.add(createModernFilterPanel(), BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainContent.getPanel(), BorderLayout.CENTER);
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createStatusPanel() {
        statusPanel = new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(PRIMARY_DARK)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .withAlignmentY(Component.CENTER_ALIGNMENT)
                .build().getPanel();

        updateStatusPanel();
        return statusPanel;
    }

    private void updateStatusPanel() {
        statusPanel.removeAll();

        JLabel completeLabel = new TLabel.Builder()
                .withForeground(Color.WHITE)
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .withText(String.format("<html><b style='color:#22c55e'>%s</b> completas</html>", completeCount))
                .build().jLabel();

        JLabel pendingLabel = new TLabel.Builder()
                .withForeground(Color.WHITE)
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .withText(String.format("<html><b style='color:#facc15'>%s</b> pendentes</html>", pendingCount))
                .build().jLabel();

        JLabel overdueLabel = new TLabel.Builder()
                .withForeground(Color.WHITE)
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .withText(String.format("<html><b style='color:#f97316'>%s</b> atrasadas</html>", overdueCount))
                .build().jLabel();

        statusPanel.add(completeLabel);
        statusPanel.add(pendingLabel);
        statusPanel.add(overdueLabel);

        statusPanel.revalidate();
        statusPanel.repaint();
    }

    private JPanel createModernHeader() {

        JPanel headerPanel = new TPanel.Builder()
                .withBorderLayout(0, 0)
                .withBackground(PRIMARY_DARK)
                .withBorder(25, 30, 25, 30)
                .build().getPanel();

        JPanel leftSection = new TPanel.Builder()
                .withFlowLayout(FlowLayout.LEFT, 0, 0)
                .withBackground(PRIMARY_DARK)
                .build().getPanel();

        JLabel titleLabel = new TLabel.Builder()
                .withText("Task Manager")
                .withForeground(Color.WHITE)
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 32)
                .build().jLabel();

        JLabel subtitleLabel = new TLabel.Builder()
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withForeground(new Color(148, 163, 184))
                .withText("Organize suas tarefas com eficiência")
                .build().jLabel();

        TPanel titleContainer = new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(PRIMARY_DARK)
                .build();

        titleContainer.add(titleLabel, subtitleLabel, Box.createVerticalStrut(5));
        leftSection.add(titleContainer.getPanel());

        JPanel buttonContainer = createHeaderButtonPanel();

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(PRIMARY_DARK);
        centerWrapper.add(statusPanel, new GridBagConstraints());

        headerPanel.add(leftSection, BorderLayout.WEST);
        headerPanel.add(centerWrapper, BorderLayout.CENTER);
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        return headerPanel;
    }


    private JPanel createHeaderButtonPanel() {
        JPanel buttonContainer = new TPanel.Builder()
                .withFlowLayout(FlowLayout.RIGHT, 12, 0)
                .withBackground(PRIMARY_DARK)
                .build().getPanel();

        JButton updateButton = new TButton.Builder()
                .asDefaultButton(SECONDARY, SECONDARY_HOVER, e -> handleUpdateTasks())
                .withText("Atualizar")
                .withPreferredSize(130, 44)
                .build().button();

        JButton createButton = new TButton.Builder()
                .asDefaultButton(ACCENT, ACCENT_HOVER, e -> createNewTask())
                .withText("+ Nova Tarefa")
                .withPreferredSize(160, 44)
                .build().button();

        buttonContainer.add(updateButton);
        buttonContainer.add(createButton);

        return buttonContainer;
    }

    private JPanel createModernFilterPanel() {
        JPanel filterPanel = new TPanel.Builder()
                .withBorderLayout(20, 0)
                .withBackground(CARD_BG)
                .withBorder(BORDER, 1, true, 20, 25, 20, 25).build().getPanel();

        JPanel leftPanel = createModernSearchPanel();
        JPanel rightPanel = createModernStatusFilterPanel();

        filterPanel.add(leftPanel, BorderLayout.WEST);
        filterPanel.add(rightPanel, BorderLayout.EAST);

        return filterPanel;
    }

    private JPanel createModernSearchPanel() {
        JPanel searchPanel = new TPanel.Builder()
                .withFlowLayout(FlowLayout.LEFT, 15, 0)
                .withBackground(CARD_BG)
                .build().getPanel();


        JLabel searchLabel = new TLabel.Builder()
                .withText("Pesquisar")
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 14)
                .withForeground(TEXT_PRIMARY)
                .build().jLabel();


        searchField = new TField.Builder()
                .withColumns(30)
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withForeground(TEXT_PRIMARY)
                .withBackground(BACKGROUND)
                .withBorder(BORDER, 1, true, 10, 16, 10, 16)
                .withText(DEFAULT_SEARCH_STRING)
                .build();

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(DEFAULT_SEARCH_STRING)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(DEFAULT_SEARCH_STRING);
                    searchField.setForeground(TEXT_SECONDARY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        return searchPanel;
    }

    private JPanel createModernStatusFilterPanel() {

        TPanel statusSelectPanel = new TPanel.Builder()
                .withFlowLayout(FlowLayout.RIGHT, 15, 0)
                .withBackground(CARD_BG)
                .build();

        JLabel statusLabel = new TLabel.Builder()
                .withText("Status")
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 14)
                .withForeground(TEXT_PRIMARY).build().jLabel();


        String[] statusOptions = {"Todas", "Pendente", DEFAULT_COMPLETE_STRING, "Incompleta", "Atrasada"};

        statusFilterCombo = new TComboBox.Builder()
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withPreferredSize(160, 44)
                .withBackground(BACKGROUND)
                .withForeground(TEXT_PRIMARY)
                .withBorder(BORDER, 1, true, 0, 12, 0, 12)
                .withOptions(statusOptions)
                .build();

        statusFilterCombo.addActionListener(e -> {
            currentStatusFilter = (String) statusFilterCombo.getSelectedItem();
            applyFilters();
        });

        JButton clearButton = new TButton.Builder()
                .asDefaultButton(new Color(148, 163, 184), new Color(100, 116, 139), e -> clearAllFilters())
                .withText("Limpar filtros")
                .withPreferredSize(130, 44)
                .build().button();

        statusSelectPanel.add(statusLabel, statusFilterCombo, clearButton);


        return statusSelectPanel.getPanel();
    }

    private void clearAllFilters() {
        searchField.setText(DEFAULT_SEARCH_STRING);
        searchField.setForeground(TEXT_SECONDARY);
        statusFilterCombo.setSelectedIndex(0);
        applyFilters();
    }

    private void applyFilters() {
        String text = searchField.getText().trim();
        if (!text.equals(DEFAULT_SEARCH_STRING)) {
            currentSearchText = text.toLowerCase();
        } else {
            currentSearchText = "";
        }
        refreshTasks();
    }

    private JPanel createTasksContainerPanel() {
        return new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(BACKGROUND).build().getPanel();
    }

    private JScrollPane createTasksScrollPane() {
        return new TScrollPane.Builder()
                .withBorder()
                .withBackground(BACKGROUND)
                .withViewPortBG(BACKGROUND)
                .withUnitIncrement(16)
                .build(taskPanel).getScrollPane();
    }

    private void handleUpdateTasks() {
        refreshTasks();
        showModernDialog("Tarefas atualizadas com sucesso!");

    }


    @SneakyThrows
    private void createNewTask() {
        JPanel panel = new TPanel.Builder()
                .withGridLayout(3, 2, 10, 15)
                .withBorder(10, 10, 10, 10)
                .build().getPanel();


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

        int option = JOptionPane.showConfirmDialog(frame, panel, "Criar Nova Tarefa", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            handleTaskCreation(descField.getText(), ownerField.getText(), daysField.getText());
            refreshTasks();
        }
    }

    private JTextField createModernTextField() {
        return new TField.Builder()
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withBorder(BORDER, 1, false, 8, 12, 8, 12)
                .build();
    }

    @SneakyThrows
    private void handleTaskCreation(String description, String ownerName, String daysStr) {
        description = description.trim();
        ownerName = ownerName.trim();
        daysStr = daysStr.trim();

        if (description.isEmpty() || ownerName.isEmpty()) {
            showModernDialog("Preencha todos os campos!");
            return;
        }

        try {

            int days = Integer.parseInt(daysStr);
            long dayMillis = 86400000L;
            long nowMillis = System.currentTimeMillis();
            long expiryDateMillis = nowMillis - (dayMillis * days);

            Task createdTask = taskService.create(new Task(ownerName, description, expiryDateMillis));


            long taskID = createdTask.getId();

            producerService.sendEvent("CREATE_TASK", taskID);
            showModernDialog("Tarefa criada com sucesso!");
            refreshTasks();
        } catch (NumberFormatException ex) {
            showModernDialog("Prazo deve ser um número inteiro!");
        }
    }

    @SneakyThrows
    public void refreshTasks() {
        Thread.sleep(100);
        SwingUtilities.invokeLater(() -> {
            taskPanel.removeAll();

            List<Task> tasks = taskService.getAll();
            completeCount = 0;
            overdueCount = 0;
            pendingCount = 0;

            tasks.forEach(task -> {
                switch (task.getStatus().toUpperCase()) {
                    case "COMPLETA" -> completeCount++;
                    case "ATRASADA" -> overdueCount++;
                    case "PENDENTE", "INCOMPLETA" -> pendingCount++;
                    default -> log.warn("[Tasks] Task status don't exists.");
                }
            });


            List<Task> filteredTasks = filterTasks(tasks);

            if (filteredTasks.isEmpty()) {
                displayModernEmptyState();
            } else {
                displayTasks(filteredTasks);
            }

            updateStatusPanel();
            taskPanel.revalidate();
            taskPanel.repaint();
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    private List<Task> filterTasks(List<Task> tasks) {
        return tasks.stream().filter(this::matchesStatusFilter).filter(this::matchesSearchFilter).toList();
    }

    private boolean matchesStatusFilter(Task task) {
        return currentStatusFilter.equalsIgnoreCase("TODAS") || task.getStatus().trim().equalsIgnoreCase(currentStatusFilter);
    }

    private boolean matchesSearchFilter(Task task) {
        if (currentSearchText.isEmpty()) return true;

        return task.getDescription().toLowerCase().contains(currentSearchText) || task.getOwnerName().toLowerCase().contains(currentSearchText) || String.valueOf(task.getId()).contains(currentSearchText);
    }

    private void displayTasks(List<Task> tasks) {
        for (Task task : tasks) {
            taskPanel.add(createModernTaskCard(task));
            taskPanel.add(Box.createVerticalStrut(16));
        }

    }

    private void displayModernEmptyState() {
        TPanel emptyPanel = new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(BACKGROUND)
                .withBorder(60, 0, 60, 0).build();

        JLabel emptyIcon = new TLabel.Builder()
                .withText("✓")
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 72)
                .withForeground(TEXT_SECONDARY)
                .withAlignmentX(Component.CENTER_ALIGNMENT).build().jLabel();

        JLabel emptyTitle = new TLabel.Builder()
                .withText("Nenhuma tarefa encontrada")
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 20)
                .withForeground(TEXT_PRIMARY)
                .withAlignmentX(Component.CENTER_ALIGNMENT).build().jLabel();


        JLabel emptyDesc = new TLabel.Builder()
                .withText("Tente ajustar os filtros ou criar uma nova tarefa")
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 14)
                .withForeground(TEXT_SECONDARY)
                .withAlignmentX(Component.CENTER_ALIGNMENT).build().jLabel();


        emptyPanel.add(
                emptyIcon, Box.createVerticalStrut(16),
                emptyTitle, Box.createVerticalStrut(8),
                emptyDesc
        );
        taskPanel.add(emptyPanel.getPanel());
    }

    private JPanel createModernTaskCard(Task task) {
        JPanel card = new TPanel.Builder()
                .withBorderLayout(20, 0)
                .withBackground(CARD_BG)
                .withBorder(BORDER, 1, true, 20, 24, 20, 24)
                .withMaximumSize(180)
                .build().getPanel();

        JPanel statusIndicator = new TPanel.Builder()
                .withPreferredSize(5, 0)
                .withBackground(getStatusColor(task.getStatus()))
                .build().getPanel();

        card.add(statusIndicator, BorderLayout.WEST);
        card.add(createModernTaskInfo(task), BorderLayout.CENTER);
        card.add(createModernActionButtons(task), BorderLayout.EAST);

        return card;
    }

    private JPanel createModernTaskInfo(Task task) {
        TPanel infoPanel = new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(CARD_BG)
                .build();


        JLabel titleLabel = new TLabel.Builder()
                .withText(task.getDescription())
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 18)
                .withForeground(TEXT_PRIMARY)
                .withAlignmentX(Component.LEFT_ALIGNMENT)
                .build().jLabel();

        TPanel metaPanel = new TPanel.Builder()
                .withFlowLayout(FlowLayout.LEFT, 0, 8)
                .withBackground(CARD_BG)
                .withAlignmentX(Component.LEFT_ALIGNMENT)
                .build();

        metaPanel.add(
                createMetaBadge("ID: " + task.getId(), new Color(241, 245, 249)), Box.createVerticalStrut(8),
                createMetaBadge(task.getOwnerName(), new Color(241, 245, 249)), Box.createVerticalStrut(8),
                createStatusBadge(task.getStatus())
        );

        TPanel datesPanel = new TPanel.Builder()
                .withFlowLayout(FlowLayout.LEFT, 0, 4)
                .withBackground(CARD_BG)
                .withAlignmentX(Component.LEFT_ALIGNMENT)
                .build();

        JLabel startDate = new TLabel.Builder()
                .withText("Início: " + formatDate(task.getStartDate()))
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 13)
                .withForeground(TEXT_SECONDARY)
                .build().jLabel();

        JLabel separator = new TLabel.Builder().withText(" • ").withForeground(TEXT_SECONDARY).build().jLabel();
        JLabel endDate = new TLabel.Builder()
                .withText("Prazo: " + formatDate(task.getExpiryDate()))
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 13)
                .withForeground(TEXT_SECONDARY)
                .build().jLabel();

        datesPanel.add(startDate, separator, endDate);
        infoPanel.add(
                titleLabel, Box.createVerticalStrut(12),
                metaPanel.getPanel(), Box.createVerticalStrut(8),
                datesPanel.getPanel()
        );

        return infoPanel.getPanel();
    }

    private JLabel createMetaBadge(String text, Color bgColor) {
        return new TLabel.Builder()
                .withText(text)
                .withFont(DEFAULT_FONT_NAME, Font.BOLD, 12)
                .withForeground(TEXT_PRIMARY)
                .withBackground(bgColor)
                .withOpaque(true)
                .withBorder(4, 12, 4, 12)
                .build().jLabel();
    }

    private JLabel createStatusBadge(String status) {
        Color statusColor = getStatusColor(status);
        Color bgColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 30);

        return new TLabel.Builder()
                .withText(status.toUpperCase())
                .withFont(DEFAULT_FONT_NAME, Font.PLAIN, 12)
                .withForeground(statusColor)
                .withOpaque(true)
                .withBackground(bgColor)
                .withBorder(4, 12, 4, 12)
                .build().jLabel();
    }

    private JPanel createModernActionButtons(Task task) {

        JButton editBtn = new TButton.Builder()
                .asDefaultButton(SECONDARY, SECONDARY_HOVER, e -> handleEditStatus(task))
                .withText("Editar")
                .withPreferredSize(120, 40)
                .withMaximumSize(120, 40)
                .withAlignmentX(Component.CENTER_ALIGNMENT).build().button();

        JButton statusBtn = new TButton.Builder()
                .asDefaultButton(new Color(139, 92, 246), new Color(124, 58, 237), e -> handleEditStatus(task))
                .withText("Status")
                .withPreferredSize(120, 40)
                .withMaximumSize(120, 40)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .build().button();


        JButton completeBtn = new TButton.Builder()
                .asDefaultButton(ACCENT, ACCENT_HOVER, e -> handleCompleteTask(task))
                .withText("Completar")
                .withHoverColor(ACCENT_HOVER)
                .withPreferredSize(120, 40)
                .withMaximumSize(120, 40)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .build().button();


        JButton deleteBtn = new TButton.Builder()
                .withText("Deletar")
                .asDefaultButton(DANGER, DANGER_HOVER, e -> handleDeleteTask(task))
                .withPreferredSize(120, 40)
                .withMaximumSize(120, 40)
                .withAlignmentX(Component.CENTER_ALIGNMENT)
                .build().button();


        TPanel buttonPanel = new TPanel.Builder()
                .withBoxLayout(BoxLayout.Y_AXIS)
                .withBackground(CARD_BG).build();

        buttonPanel.add(
                editBtn, Box.createVerticalStrut(8),
                statusBtn, Box.createVerticalStrut(8),
                completeBtn, Box.createVerticalStrut(8),
                deleteBtn, Box.createVerticalStrut(8)
        );


        return buttonPanel.getPanel();
    }

    @SneakyThrows
    private void handleEditDescription(Task task) {
        String newDescription = (String) JOptionPane.showInputDialog(frame, "Nova descrição:", "Editar Tarefa", JOptionPane.PLAIN_MESSAGE, null, null, task.getDescription());

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            producerService.sendEvent(task, "UPDATE_DESCRIPTION", newDescription.trim());
            showModernDialog("Descrição atualizada!");
            refreshTasks();
        }
    }

    private void handleEditStatus(Task task) {
        String[] options = {"Pendente", DEFAULT_COMPLETE_STRING, "Incompleta", "Atrasada"};
        String newStatus = (String) JOptionPane.showInputDialog(frame, "Escolha o novo status:", "Alterar Status", JOptionPane.PLAIN_MESSAGE, null, options, task.getStatus());

        if (newStatus != null) {
            producerService.sendEvent(task, "UPDATE_STATUS", newStatus.trim());
            showModernDialog("Status atualizado!");
            refreshTasks();
        }
    }

    private void handleCompleteTask(Task task) {
        int option = JOptionPane.showConfirmDialog(frame, "Deseja realmente completar esta tarefa?", "Confirmação", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            producerService.sendEvent(task, "UPDATE_STATUS", DEFAULT_COMPLETE_STRING);
            log.info("[Tasks] Task with id: {} has been completed.", task.getId());
            refreshTasks();
        }
    }

    @SneakyThrows
    private void handleDeleteTask(Task task) {
        int option = JOptionPane.showConfirmDialog(frame, "Deseja realmente deletar esta tarefa? Esta ação não pode ser desfeita.", "Confirmação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            producerService.sendEvent(task, "DELETE_TASK", String.valueOf(task.getId()));
            log.info("[Tasks] Task with ID: {} has been deleted.", task.getId());
            refreshTasks();
        }
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date(timestamp));
    }

    private void showModernDialog(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(() -> {
            setupMainFrame();
            buildUI();
            refreshTasks();
            show();
        });
    }

}