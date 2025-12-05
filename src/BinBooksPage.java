// BinBooksPage.java
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class BinBooksPage extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRestore, btnDeletePerm, btnRefresh;
    private LibraryDashboard parent;

    public BinBooksPage(LibraryDashboard parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        JLabel title = new JLabel("Deleted Books (Bin)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(220, 53, 69));
        topPanel.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);

        btnRefresh = createBtn("Refresh", new Color(40, 167, 69));
        btnRefresh.addActionListener(e -> loadBinBooks());

        btnRestore = createBtn("Restore Selected", new Color(0, 123, 255));
        btnRestore.addActionListener(e -> restoreBook());

        btnDeletePerm = createBtn("Delete Permanently", new Color(220, 53, 69));
        btnDeletePerm.addActionListener(e -> deletePermanently());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnRestore);
        btnPanel.add(btnDeletePerm);

        topPanel.add(btnPanel, BorderLayout.EAST);

        // Table
        model = new DefaultTableModel(new Object[]{
            "Book ID", "Title", "Author", "Deleted On"
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
        table.getTableHeader().setBackground(new Color(108, 117, 125));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Back Button
        JButton back = new JButton("Back to Dashboard");
        back.setFont(new Font("Segoe UI", Font.BOLD, 15));
        back.setBackground(new Color(108, 117, 125));
        back.setForeground(Color.WHITE);
        back.addActionListener(e -> parent.switchPage("DashboardHome"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(248, 250, 252));
        bottom.setBorder(BorderFactory.createEmptyBorder(15, 25, 20, 25));
        bottom.add(back);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadBinBooks();
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 48));
        return btn;
    }

    private void loadBinBooks() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT book_id, title, author, deleted_at FROM bin_books ORDER BY deleted_at DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getTimestamp("deleted_at").toLocalDateTime().toString().substring(0, 19).replace("T", " ")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading bin: " + ex.getMessage());
        }
    }

    private void restoreBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to restore", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookId = (String) model.getValueAt(row, 0);
        String title = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Restore book:\n" + bookId + " - " + title + "\n\nThis will make it available again.",
            "Restore Book?", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            con.setAutoCommit(false);

            // Get book data
            PreparedStatement get = con.prepareStatement("SELECT * FROM bin_books WHERE book_id = ?");
            get.setString(1, bookId);
            ResultSet rs = get.executeQuery();
            if (!rs.next()) throw new Exception("Book not found in bin");

            // Restore to books table
            String insert = "INSERT INTO books (book_id, title, author, publisher, year, category, available) VALUES (?, ?, ?, ?, ?, ?, 'Yes')";
            PreparedStatement pst = con.prepareStatement(insert);
            pst.setString(1, rs.getString("book_id"));
            pst.setString(2, rs.getString("title"));
            pst.setString(3, rs.getString("author"));
            pst.setString(4, rs.getString("publisher"));
            pst.setObject(5, rs.getObject("year"));
            pst.setString(6, rs.getString("category"));
            pst.executeUpdate();

            // Delete from bin
            PreparedStatement del = con.prepareStatement("DELETE FROM bin_books WHERE book_id = ?");
            del.setString(1, bookId);
            del.executeUpdate();

            con.commit();

            JOptionPane.showMessageDialog(this, "Book restored successfully!\n" + bookId, "Restored", JOptionPane.INFORMATION_MESSAGE);
            loadBinBooks();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage());
        }
    }

    private void deletePermanently() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete permanently", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookId = (String) model.getValueAt(row, 0);
        String title = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>PERMANENT DELETE</b><br><br>Book: " + bookId + "<br>Title: " + title +
            "<br><br><font color='red'>This action cannot be undone!</font></html>",
            "Delete Permanently?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            PreparedStatement pst = con.prepareStatement("DELETE FROM bin_books WHERE book_id = ?");
            pst.setString(1, bookId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book deleted permanently!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadBinBooks();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}