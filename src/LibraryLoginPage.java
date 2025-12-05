import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryLoginPage extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin, btnClear;
    private JLabel lblStatus;

    public LibraryLoginPage() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(240, 248, 255));
        setContentPane(bg);

        RoundedPanel card = new RoundedPanel(30, Color.WHITE);
        card.setPreferredSize(new Dimension(500, 480));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(title, c);

        JLabel sub = new JLabel("Librarian Login");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(Color.GRAY);
        c.gridy = 1;
        card.add(sub, c);

        // Username
        c.gridwidth = 1; c.gridy = 2; c.anchor = GridBagConstraints.WEST;
        card.add(new JLabel("Username:"), c);
        txtUser = new JTextField(20);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.gridx = 1;
        card.add(txtUser, c);

        // Password
        c.gridx = 0; c.gridy = 3;
        card.add(new JLabel("Password:"), c);
        txtPass = new JPasswordField(20);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.gridx = 1;
        card.add(txtPass, c);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        btnLogin = styledButton("Login", new Color(40, 167, 69));
        btnClear = styledButton("Clear", new Color(220, 53, 69));
        btnPanel.add(btnLogin);
        btnPanel.add(btnClear);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        card.add(btnPanel, c);

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 5;
        card.add(lblStatus, c);

        bg.add(card);

        // Actions
        btnLogin.addActionListener(e -> login());
        btnClear.addActionListener(e -> clear());
        txtPass.addActionListener(e -> login());

        setVisible(true);
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(140, 45));
        return b;
    }

    private void clear() {
        txtUser.setText("");
        txtPass.setText("");
        lblStatus.setText(" ");
    }

    private void login() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Please fill both fields");
            return;
        }

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_db", "root", "")) {

            String sql = "SELECT * FROM librarians WHERE username=? AND password=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + rs.getString("full_name"));
                new LibraryDashboard().setVisible(true);
                dispose();
            } else {
                lblStatus.setText("Invalid username or password");
            }
        } catch (Exception ex) {
            lblStatus.setText("Database Error");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryLoginPage());
    }

    // Rounded Panel
    static class RoundedPanel extends JPanel {
        private int radius;
        private Color bg;

        public RoundedPanel(int r, Color c) {
            radius = r; bg = c; setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,20));
            g2.fillRoundRect(8, 8, getWidth()-16, getHeight()-16, radius, radius);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth()-8, getHeight()-8, radius, radius);
        }
    }
}