// LibraryDashboard.java
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class LibraryDashboard extends JFrame {

    private JPanel sidebar, contentPanel;
    private CardLayout cardLayout;
    private JButton selectedButton = null;

    // Colors - Modern & Professional
    private static final Color SIDEBAR_BG = new Color(33, 37, 41);
    private static final Color HOVER_BG = new Color(52, 58, 64);
    private static final Color SELECTED_BG = new Color(70, 75, 80);
    private static final Color PRIMARY = new Color(0, 123, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color BG_LIGHT = new Color(248, 250, 252);

    public LibraryDashboard() {
        setTitle("Library Management System - Dashboard");
        setSize(1300, 800);
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= LEFT SIDEBAR =================
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(300, 0));

        // Logo
        JLabel logo = new JLabel("Library Pro", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(Color.WHITE);
        logo.setBackground(new Color(0, 105, 217));
        logo.setOpaque(true);
        logo.setBorder(new EmptyBorder(30, 0, 40, 0));
        sidebar.add(logo, BorderLayout.NORTH);

        // ================= MENU CENTER =================
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(SIDEBAR_BG);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        String[] items = {
                "Dashboard Home",
                "Add New Book",
                "Issue Book",
                "Return Book",
                "View All Books",
                "Issued Books"
        };

        for (String item : items) {
            JButton btn = createMenuButton(item);

            btn.addActionListener(e -> {
                switchPage(item.replace(" ", "")
                                .replace("(", "")
                                .replace(")", ""));

                if (selectedButton != null && selectedButton != btn) {
                    selectedButton.setBackground(SIDEBAR_BG);
                }

                btn.setBackground(SELECTED_BG);
                selectedButton = btn;
            });

            menuPanel.add(btn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 12))); // spacing
        }

        menuPanel.add(Box.createVerticalGlue());

        // ================= LOGOUT BUTTON BOTTOM =================
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(SIDEBAR_BG);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JButton logout = createMenuButton("Logout");
        logout.setBackground(DANGER);

        logout.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Logout from system?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION);

            if (opt == JOptionPane.YES_OPTION) {
                new LibraryLoginPage().setVisible(true);
                dispose();
            }
        });

        logout.setMaximumSize(new Dimension(260, 50));
        bottomPanel.add(logout);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        sidebar.add(new JScrollPane(menuPanel), BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        // ================= CONTENT PANEL =================
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(BG_LIGHT);

        // Add Pages
        contentPanel.add(new HomePanel(), "DashboardHome");
        contentPanel.add(new AddBookPage(this), "AddNewBook");
        contentPanel.add(new IssueBookPage(this), "IssueBook");
        contentPanel.add(new ReturnBookPage(this), "ReturnBook");
        contentPanel.add(new ViewAllBooksPage(this), "ViewAllBooks");
        contentPanel.add(new IssuedBooksPage(this), "IssuedBooks");
        contentPanel.add(new BinBooksPage(this), "DeletedBooksBin");
        contentPanel.add(new Placeholder("Bin - Deleted Books"), "DeletedBooksBin");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "DashboardHome");
        setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(260, 55));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != selectedButton && !text.equals("Logout"))
                    btn.setBackground(HOVER_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != selectedButton && !text.equals("Logout"))
                    btn.setBackground(SIDEBAR_BG);
            }
        });

        return btn;
    }

    public void switchPage(String pageKey) {
        cardLayout.show(contentPanel, pageKey);
    }

    // ================= HOME PAGE =================
    private class HomePanel extends JPanel {
        public HomePanel() {
            setBackground(BG_LIGHT);
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(30, 30, 30, 30);

            JLabel title = new JLabel("Welcome to Library Management System");
            title.setFont(new Font("Segoe UI", Font.BOLD, 42));
            title.setForeground(PRIMARY);
            gbc.gridx = 0; gbc.gridy = 0;
            add(title, gbc);

            JLabel subtitle = new JLabel("Manage books, issue & return with ease");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            subtitle.setForeground(new Color(100, 100, 100));
            gbc.gridy = 1; gbc.insets = new Insets(10, 30, 50, 30);
            add(subtitle, gbc);

            JPanel stats = new JPanel(new GridLayout(1, 4, 30, 0));
            stats.setOpaque(false);

            gbc.gridy = 2; 
            add(stats, gbc);
        }

        private JPanel createStatCard(String title, String value, Color color) {
            JPanel card = new JPanel();
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            card.setPreferredSize(new Dimension(250, 140));
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(20, 20, 30, 20));

            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 48));
            lblValue.setForeground(color);
            lblValue.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            lblTitle.setForeground(Color.GRAY);

            card.add(lblValue, BorderLayout.CENTER);
            card.add(lblTitle, BorderLayout.SOUTH);

            return card;
        }
    }

    // Placeholder
    private class Placeholder extends JPanel {
        public Placeholder(String name) {
            setBackground(BG_LIGHT);
            setLayout(new GridBagLayout());
            JLabel lbl = new JLabel(name + " - Coming in Next Update", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
            lbl.setForeground(new Color(150, 150, 150));
            add(lbl);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryDashboard::new);
    }
}
