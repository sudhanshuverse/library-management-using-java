// IssueBookPage.java
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class IssueBookPage extends JPanel {

    private JTextField tfStudentRoll, tfStudentName, tfBookId, tfBookTitle;
    private JLabel lblAvailability, lblIssueDate, lblDueDate;
    private JButton btnSearchStudent, btnSearchBook, btnIssue;
    private LibraryDashboard parent;

    public IssueBookPage(LibraryDashboard parent) {
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
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> parent.switchPage("DashboardHome"));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(back, gbc);

        // Title
        JLabel title = new JLabel("Issue Book to Student");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(0, 123, 255));
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        // Student Section
        createLabel("Student Roll No :", gbc, 1);
        tfStudentRoll = createTextField(gbc, 1);
        btnSearchStudent = createSmallButton("Search");
        btnSearchStudent.addActionListener(e -> searchStudent());
        gbc.gridx = 2; gbc.gridwidth = 1;
        add(btnSearchStudent, gbc);

        tfStudentName = createTextField(gbc, 2);
        tfStudentName.setEditable(false);

        // Book Section
        createLabel("Book ID :", gbc, 3);
        tfBookId = createTextField(gbc, 3);
        btnSearchBook = createSmallButton("Search Book");
        btnSearchBook.addActionListener(e -> searchBook());
        gbc.gridx = 2;
        add(btnSearchBook, gbc);

        tfBookTitle = createTextField(gbc, 4);
        tfBookTitle.setEditable(false);

        lblAvailability = new JLabel("Availability: Not checked");
        lblAvailability.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        add(lblAvailability, gbc);

        // Dates
        lblIssueDate = new JLabel("Issue Date: " + java.time.LocalDate.now());
        lblDueDate = new JLabel("Due Date: " + java.time.LocalDate.now().plusDays(14));
        lblIssueDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDueDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 6;
        add(lblIssueDate, gbc);
        gbc.gridy = 7;
        add(lblDueDate, gbc);

        // Issue Button
        btnIssue = new JButton("Issue Book Now");
        btnIssue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnIssue.setBackground(new Color(40, 167, 69));
        btnIssue.setForeground(Color.WHITE);
        btnIssue.setPreferredSize(new Dimension(380, 62));
        btnIssue.setEnabled(false);
        btnIssue.addActionListener(e -> issueBook());

        // Hover
        btnIssue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnIssue.isEnabled())
                    btnIssue.setBackground(new Color(30, 140, 55));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btnIssue.isEnabled())
                    btnIssue.setBackground(new Color(40, 167, 69));
            }
        });

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(40, 0, 50, 0);
        add(btnIssue, gbc);
    }

    private void createLabel(String text, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = row;
        add(label, gbc);
    }

    private JTextField createTextField(GridBagConstraints gbc, int row) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(380, 48));
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1;
        add(field, gbc);
        return field;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(0, 123, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // Search Student (You can link to a students table later)
    private void searchStudent() {
        String roll = tfStudentRoll.getText().trim();
        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Student Roll No", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // For now, accept any roll no
        tfStudentName.setText("Student Name - Roll: " + roll);
        checkIfCanIssue();
    }

    // Search Book
    private void searchBook() {
        String bookId = tfBookId.getText().trim();
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Book ID", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT title, available FROM books WHERE book_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, bookId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                tfBookTitle.setText(rs.getString("title"));
                String avail = rs.getString("available");
                lblAvailability.setText("Availability: " + (avail.equals("Yes") ? "Available" : "Already Issued"));
                lblAvailability.setForeground(avail.equals("Yes") ? Color.GREEN.darker() : Color.RED);

                if (avail.equals("Yes")) {
                    checkIfCanIssue();
                } else {
                    btnIssue.setEnabled(false);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Book not found!", "Error", JOptionPane.ERROR_MESSAGE);
                tfBookTitle.setText("");
                lblAvailability.setText("Availability: Not found");
                lblAvailability.setForeground(Color.RED);
                btnIssue.setEnabled(false);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void checkIfCanIssue() {
        if (!tfStudentName.getText().isEmpty() && 
            !tfBookTitle.getText().isEmpty() && 
            lblAvailability.getText().contains("Available")) {
            btnIssue.setEnabled(true);
        }
    }

    private void issueBook() {
        String roll = tfStudentRoll.getText().trim();
        String name = tfStudentName.getText();
        String bookId = tfBookId.getText().trim();
        String bookTitle = tfBookTitle.getText();

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            con.setAutoCommit(false);

            // Update books table
            String updateBook = "UPDATE books SET available='No', issued_to_roll=?, issued_to_name=?, issue_date=?, due_date=? WHERE book_id=?";
            PreparedStatement pst1 = con.prepareStatement(updateBook);
            pst1.setString(1, roll);
            pst1.setString(2, name);
            pst1.setDate(3, Date.valueOf(java.time.LocalDate.now()));
            pst1.setDate(4, Date.valueOf(java.time.LocalDate.now().plusDays(14)));
            pst1.setString(5, bookId);
            pst1.executeUpdate();

            // Insert into issued_books history
            String insertHistory = "INSERT INTO issued_books (book_id, book_title, student_roll, student_name, issue_date, due_date) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst2 = con.prepareStatement(insertHistory);
            pst2.setString(1, bookId);
            pst2.setString(2, bookTitle);
            pst2.setString(3, roll);
            pst2.setString(4, name);
            pst2.setDate(5, Date.valueOf(java.time.LocalDate.now()));
            pst2.setDate(6, Date.valueOf(java.time.LocalDate.now().plusDays(14)));
            pst2.executeUpdate();

            con.commit();

            JOptionPane.showMessageDialog(this,
                "<html><h2>Book Issued Successfully!</h2><p>Due Date: <b>" + 
                java.time.LocalDate.now().plusDays(14) + "</b></p></html>",
                "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Issue Failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        tfStudentRoll.setText("");
        tfStudentName.setText("");
        tfBookId.setText("");
        tfBookTitle.setText("");
        lblAvailability.setText("Availability: Not checked");
        btnIssue.setEnabled(false);
        tfStudentRoll.requestFocus();
    }
}