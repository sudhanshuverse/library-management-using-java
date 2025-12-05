import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddBookPage extends JPanel {

    private JTextField tfBookId, tfTitle, tfAuthor, tfPublisher, tfYear;
    private JComboBox<String> cbCategory;
    private JButton btnSave;
    private LibraryDashboard parent;

    public AddBookPage(LibraryDashboard parent) {
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(back, gbc);

        // Title
        JLabel title = new JLabel("Add New Book");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(0, 123, 255));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        // Form Fields
        tfBookId = createField("Book ID (Auto Generated)", gbc, 1);
        tfBookId.setEditable(false);
        tfBookId.setText(generateNextBookId()); // Auto ID

        tfTitle = createField("Book Title *", gbc, 2);
        tfAuthor = createField("Author Name *", gbc, 3);
        tfPublisher = createField("Publisher", gbc, 4);
        tfYear = createField("Publication Year", gbc, 5);

        // Category
        JLabel lblCat = new JLabel("Category :");
        lblCat.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(lblCat, gbc);

        String[] categories = {
                "", "Fiction", "Non-Fiction", "Science", "Technology", "History",
                "Biography", "Children", "Reference", "Poetry", "Self-Help", "Romance", "Thriller"
        };
        cbCategory = new JComboBox<>(categories);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbCategory.setPreferredSize(new Dimension(420, 48));
        cbCategory.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(cbCategory, gbc);

        // Save Button
        btnSave = new JButton("Add Book to Library");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(380, 62));

        // Hover effect
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(new Color(30, 140, 55));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(new Color(40, 167, 69));
            }
        });

        btnSave.addActionListener(e -> saveBook());

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(40, 0, 50, 0);
        add(btnSave, gbc);
    }

    private JTextField createField(String labelText, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = row;
        add(label, gbc);

        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(420, 48));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(field, gbc);
        return field;
    }

    // Generate next Book ID: LIB-1001, LIB-1002...
    private String generateNextBookId() {
        String nextId = "LIB-1001";
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT book_id FROM books ORDER BY id DESC LIMIT 1";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String last = rs.getString("book_id");
                int num = Integer.parseInt(last.split("-")[1]) + 1;
                nextId = "LIB-" + num;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Auto ID Error: " + e.getMessage());
        }
        return nextId;
    }

    private void saveBook() {
        String title = tfTitle.getText().trim();
        String author = tfAuthor.getText().trim();
        String publisher = tfPublisher.getText().trim();
        String yearStr = tfYear.getText().trim();
        String category = (String) cbCategory.getSelectedItem();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Author are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (category == null || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a category", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!yearStr.isEmpty() && !yearStr.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "Year must be 4 digits", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "INSERT INTO books (book_id, title, author, publisher, year, category, available) VALUES (?, ?, ?, ?, ?, ?, 'Yes')";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tfBookId.getText());
            pst.setString(2, title);
            pst.setString(3, author);
            pst.setString(4, publisher.isEmpty() ? null : publisher);
            pst.setString(5, yearStr.isEmpty() ? null : yearStr);
            pst.setString(6, category);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "<html><h2>Book Added Successfully!</h2><p><b>Book ID:</b> " + tfBookId.getText() + "</p></html>",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Reset form
            tfBookId.setText(generateNextBookId());
            tfTitle.setText("");
            tfAuthor.setText("");
            tfPublisher.setText("");
            tfYear.setText("");
            cbCategory.setSelectedIndex(0);
            tfTitle.requestFocus();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(this, "Book ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}