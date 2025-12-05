// DeleteBookPage.java
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DeleteBookPage extends JPanel {

    private JTextField tfBookId;
    private JLabel lblTitle, lblAuthor, lblCategory, lblStatus;
    private JButton btnSearch, btnDelete;
    private LibraryDashboard parent;

    public DeleteBookPage(LibraryDashboard parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBackground(new Color(248, 250, 252));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 25, 15, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Back Button
        JButton back = new JButton("Back to Dashboard");
        back.setFont(new Font("Segoe UI", Font.BOLD, 15));
        back.setBackground(new Color(0, 123, 255));
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.addActionListener(e -> parent.switchPage("DashboardHome"));
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(back, gbc);

        // Title
        JLabel title = new JLabel("Delete Book from Library");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(220, 53, 69));
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        // Book ID Search
        JLabel lblId = new JLabel("Enter Book ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 1;
        add(lblId, gbc);

        tfBookId = new JTextField();
        tfBookId.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tfBookId.setPreferredSize(new Dimension(380, 48));
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(tfBookId, gbc);

        btnSearch = new JButton("Search Book");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> searchBook());
        gbc.gridx = 3;
        add(btnSearch, gbc);

        // Details Panel
        JPanel detailsPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        detailsPanel.setBackground(new Color(248, 250, 252));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), "Book Details", 0, 0,
            new Font("Segoe UI", Font.BOLD, 18), new Color(0, 123, 255)));

        lblTitle = createDetailLabel("");
        lblAuthor = createDetailLabel("");
        lblCategory = createDetailLabel("");
        lblStatus = createDetailLabel("");

        detailsPanel.add(lblTitle);
        detailsPanel.add(lblAuthor);
        detailsPanel.add(lblCategory);
        detailsPanel.add(lblStatus);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.insets = new Insets(30, 25, 20, 25);
        add(detailsPanel, gbc);

        // Delete Button
        btnDelete = new JButton("Delete Book Permanently");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(380, 62));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteBook());

        // Hover
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnDelete.isEnabled())
                    btnDelete.setBackground(new Color(200, 30, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btnDelete.isEnabled())
                    btnDelete.setBackground(new Color(220, 53, 69));
            }
        });

        gbc.gridy = 3; gbc.insets = new Insets(40, 0, 50, 0);
        add(btnDelete, gbc);
    }

    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        return lbl;
    }

    private void searchBook() {
        String bookId = tfBookId.getText().trim();
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Book ID", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT title, author, category, available FROM books WHERE book_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, bookId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lblTitle.setText("Title      : " + rs.getString("title"));
                lblAuthor.setText("Author     : " + rs.getString("author"));
                lblCategory.setText("Category   : " + rs.getString("category"));
                String status = rs.getString("available").equals("Yes") ? "Available" : "Currently Issued";
                lblStatus.setText("Status     : " + status);

                btnDelete.setEnabled(true);
                if (!rs.getString("available").equals("Yes")) {
                    JOptionPane.showMessageDialog(this,
                        "Warning: This book is currently issued!\nIt will be moved to Bin instead of permanent delete.",
                        "Issued Book", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Book not found!", "Error", JOptionPane.ERROR_MESSAGE);
                clearDetails();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        String bookId = tfBookId.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Delete Book?</b><br><br>Book ID: " + bookId + "<br><br>This will move the book to Bin (Soft Delete)</html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            con.setAutoCommit(false);

            // Copy to bin
            String copy = "INSERT INTO bin_books (book_id, title, author, publisher, year, category) " +
                          "SELECT book_id, title, author, publisher, year, category FROM books WHERE book_id = ?";
            PreparedStatement pst1 = con.prepareStatement(copy);
            pst1.setString(1, bookId);
            pst1.executeUpdate();

            // Delete from main table
            String del = "DELETE FROM books WHERE book_id = ?";
            PreparedStatement pst2 = con.prepareStatement(del);
            pst2.setString(1, bookId);
            int deleted = pst2.executeUpdate();

            con.commit();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this,
                    "Book deleted successfully!\nMoved to Bin for recovery.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearDetails();
                tfBookId.setText("");
                tfBookId.requestFocus();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    private void clearDetails() {
        lblTitle.setText("");
        lblAuthor.setText("");
        lblCategory.setText("");
        lblStatus.setText("");
        btnDelete.setEnabled(false);
    }
}