// ReturnBookPage.java - FIXED VERSION
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBookPage extends JPanel {

    private JTextField tfSearch;
    private JTextArea taDetails;
    private JButton btnSearch, btnReturn;
    private LibraryDashboard parent;

    private String currentBookId = "";
    private LocalDate issueDate, dueDate;

    public ReturnBookPage(LibraryDashboard parent) {
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
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(back, gbc);

        // Title
        JLabel title = new JLabel("Return Book");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(220, 53, 69));
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);

        // Search Label
        JLabel lblSearch = new JLabel("Enter Student Roll No:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(lblSearch, gbc);

        // Search Field
        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tfSearch.setPreferredSize(new Dimension(380, 52));
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(tfSearch, gbc);

        // Search Button
        btnSearch = new JButton("Search Issued Books");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchByStudentRoll());
        gbc.gridx = 3; gbc.gridwidth = 1;
        add(btnSearch, gbc);

        // Details Area
        taDetails = new JTextArea(14, 60);
        taDetails.setFont(new Font("Consolas", Font.PLAIN, 15));
        taDetails.setEditable(false);
        taDetails.setBackground(Color.WHITE);
        taDetails.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        JScrollPane scroll = new JScrollPane(taDetails);
        scroll.setPreferredSize(new Dimension(700, 320));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.insets = new Insets(25, 25, 20, 25);
        add(scroll, gbc);

        // Return Button
        btnReturn = new JButton("Return Selected Book");
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnReturn.setBackground(new Color(40, 167, 69));
        btnReturn.setForeground(Color.WHITE);
        btnReturn.setPreferredSize(new Dimension(420, 68));
        btnReturn.setEnabled(false);
        btnReturn.addActionListener(e -> returnBook());

        btnReturn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnReturn.isEnabled())
                    btnReturn.setBackground(new Color(30, 140, 55));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btnReturn.isEnabled())
                    btnReturn.setBackground(new Color(40, 167, 69));
            }
        });

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.insets = new Insets(30, 0, 50, 0);
        add(btnReturn, gbc);
    }

    private void searchByStudentRoll() {
        String roll = tfSearch.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Student Roll No", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = """
                SELECT b.book_id, b.title, b.author, i.student_name, i.issue_date, i.due_date
                FROM books b
                JOIN issued_books i ON b.book_id = i.book_id
                WHERE i.student_roll = ? AND i.return_date IS NULL
                ORDER BY i.due_date ASC
                """;

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, roll);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                currentBookId = rs.getString("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String name = rs.getString("student_name");
                issueDate = rs.getDate("issue_date").toLocalDate();
                dueDate = rs.getDate("due_date").toLocalDate();

                LocalDate today = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                int fine = overdueDays > 0 ? (int) overdueDays * 5 : 0;

                String details = String.format(
                    "═══════ BOOK RETURN DETAILS ═══════\n\n" +
                    "Student Roll : %s\n" +
                    "Student Name : %s\n\n" +
                    "Book ID      : %s\n" +
                    "Title        : %s\n" +
                    "Author       : %s\n\n" +
                    "Issue Date   : %s\n" +
                    "Due Date     : %s\n" +
                    "Today        : %s\n" +
                    "Overdue      : %d days\n" +
                    "Fine         : ₹%d (₹5 per day)\n\n" +
                    "Click 'Return Selected Book' to complete return.",
                    roll, name, currentBookId, title, author,
                    issueDate, dueDate, today,
                    overdueDays > 0 ? overdueDays : 0, fine
                );

                taDetails.setText(details);
                taDetails.setForeground(overdueDays > 0 ? Color.RED.darker() : new Color(0, 100, 0));
                btnReturn.setEnabled(true);

            } else {
                taDetails.setText("No issued book found for Roll No: " + roll);
                taDetails.setForeground(Color.RED);
                btnReturn.setEnabled(false);
                currentBookId = "";
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void returnBook() {
        if (currentBookId.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm return of book:\n" + currentBookId + "\nFine will be recorded.",
            "Return Book?", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            con.setAutoCommit(false);

            // Update books table
            String sql1 = "UPDATE books SET available='Yes', issued_to_roll=NULL, issued_to_name=NULL, issue_date=NULL, due_date=NULL WHERE book_id=?";
            PreparedStatement pst1 = con.prepareStatement(sql1);
            pst1.setString(1, currentBookId);
            pst1.executeUpdate();

            // Update issued_books
            LocalDate today = LocalDate.now();
            long overdue = ChronoUnit.DAYS.between(dueDate, today);
            int fine = overdue > 0 ? (int) overdue * 5 : 0;

            String sql2 = "UPDATE issued_books SET return_date=?, fine=?, status='Returned' WHERE book_id=? AND return_date IS NULL";
            PreparedStatement pst2 = con.prepareStatement(sql2);
            pst2.setDate(1, Date.valueOf(today));
            pst2.setDouble(2, fine);
            pst2.setString(3, currentBookId);
            pst2.executeUpdate();

            con.commit();

            JOptionPane.showMessageDialog(this,
                "<html><h2>Book Returned Successfully!</h2><p>Fine: <b>₹" + fine + "</b></p></html>",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Return failed: " + ex.getMessage());
        }
    }

    private void clearForm() {
        tfSearch.setText("");
        taDetails.setText("");
        btnReturn.setEnabled(false);
        currentBookId = "";
        tfSearch.requestFocus();
    }
}