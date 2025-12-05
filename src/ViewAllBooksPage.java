// ViewAllBooksPage.java
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ViewAllBooksPage extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField tfSearch;
    private JButton btnRefresh;
    private LibraryDashboard parent;

    public ViewAllBooksPage(LibraryDashboard parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(248,  250, 252));

        // Top Panel - Title + Search + Refresh
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        // Title
        JLabel title = new JLabel("All Books in Library");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(0, 123, 255));
        topPanel.add(title, BorderLayout.WEST);

        // Search + Refresh Panel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        tfSearch = new JTextField(25);
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfSearch.setPreferredSize(new Dimension(300, 44));
        tfSearch.setToolTipText("Search by Title, Author, Book ID, Category...");
        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchBooks();
            }
        });

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRefresh.setBackground(new Color(40, 167, 69));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadAllBooks());

        rightPanel.add(new JLabel("Search:"));
        rightPanel.add(tfSearch);
        rightPanel.add(btnRefresh);

        topPanel.add(rightPanel, BorderLayout.EAST);

        // Table with Scroll
        model = new DefaultTableModel(new Object[]{
            "Book ID", "Title", "Author", "Publisher", "Year", "Category", "Status", "Issued To"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(38);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(0, 123, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(new Color(220, 220, 220));

        // Column widths
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(100);  // Book ID
        columns.getColumn(1).setPreferredWidth(300);  // Title
        columns.getColumn(2).setPreferredWidth(180);  // Author
        columns.getColumn(5).setPreferredWidth(120);  // Category
        columns.getColumn(6).setPreferredWidth(100);  // Status
        columns.getColumn(7).setPreferredWidth(200);  // Issued To

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Back Button (Bottom)
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
        loadAllBooks();
    }

    private void loadAllBooks() {
        model.setRowCount(0); // Clear table

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT book_id, title, author, publisher, year, category, available, issued_to_name FROM books ORDER BY created_at DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String status = rs.getString("available").equals("Yes") ? "Available" : "Issued";
                String issuedTo = rs.getString("issued_to_name");
                if (issuedTo == null || issuedTo.isEmpty()) issuedTo = "-";

                model.addRow(new Object[]{
                    rs.getString("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher") != null ? rs.getString("publisher") : "-",
                    rs.getString("year") != null ? rs.getString("year") : "-",
                    rs.getString("category"),
                    status,
                    issuedTo
                });
            }

            // Update row count in title or label if you want
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String query = tfSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadAllBooks();
            return;
        }

        model.setRowCount(0);

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT book_id, title, author, publisher, year, category, available, issued_to_name FROM books " +
                         "WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR book_id LIKE ? OR category LIKE ?";

            PreparedStatement pst = con.prepareStatement(sql);
            String like = "%" + query + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            pst.setString(3, like);
            pst.setString(4, like);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String status = rs.getString("available").equals("Yes") ? "Available" : "Issued";
                String issuedTo = rs.getString("issued_to_name");
                if (issuedTo == null) issuedTo = "-";

                model.addRow(new Object[]{
                    rs.getString("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher") != null ? rs.getString("publisher") : "-",
                    rs.getString("year") != null ? rs.getString("year") : "-",
                    rs.getString("category"),
                    status,
                    issuedTo
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage());
        }
    }
}