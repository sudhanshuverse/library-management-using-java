// IssuedBooksPage.java
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class IssuedBooksPage extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private LibraryDashboard parent;

    public IssuedBooksPage(LibraryDashboard parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // Top Panel - Title + Refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JLabel title = new JLabel("Issued Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(220, 53, 69)); // Red accent
        topPanel.add(title, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh List");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setBackground(new Color(40, 167, 69));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadIssuedBooks());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(btnRefresh);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Table Setup
        model = new DefaultTableModel(new Object[]{
            "Book ID", "Title", "Student Roll", "Student Name", "Issue Date", "Due Date", "Days Overdue", "Fine (₹5/day)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(220, 53, 69));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(255, 205, 210));
        table.setGridColor(new Color(220, 220, 220));

        // Custom renderer for overdue rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String dueDateStr = (String) table.getValueAt(row, 5);
                String daysOverdueStr = (String) table.getValueAt(row, 6);

                LocalDate dueDate = LocalDate.parse(dueDateStr);
                long overdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());

                if (overdue > 0) {
                    if (!isSelected) c.setBackground(new Color(255, 205, 210)); // Light red
                    c.setForeground(Color.RED.darker());
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    if (!isSelected) c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Back Button
        JButton back = new JButton("Back to Dashboard");
        back.setFont(new Font("Segoe UI", Font.BOLD, 15));
        back.setBackground(new Color(108, 117, 125));
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.addActionListener(e -> parent.switchPage("DashboardHome"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 20, 25));
        bottomPanel.add(back);

        // Add all
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data on start
        loadIssuedBooks();
    }

    private void loadIssuedBooks() {
        model.setRowCount(0);

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = """
                SELECT i.book_id, b.title, i.student_roll, i.student_name,
                       i.issue_date, i.due_date
                FROM issued_books i
                JOIN books b ON i.book_id = b.book_id
                WHERE i.return_date IS NULL
                ORDER BY i.due_date ASC
                """;

            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String bookId = rs.getString("book_id");
                String title = rs.getString("title");
                String roll = rs.getString("student_roll");
                String name = rs.getString("student_name");
                LocalDate issueDate = rs.getDate("issue_date").toLocalDate();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();

                LocalDate today = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                int fine = overdueDays > 0 ? (int) overdueDays * 5 : 0;

                model.addRow(new Object[]{
                    bookId,
                    title,
                    roll,
                    name,
                    issueDate.toString(),
                    dueDate.toString(),
                    overdueDays > 0 ? overdueDays + " days" : "On Time",
                    "₹" + fine
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading issued books: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}