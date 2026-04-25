package com.atm.gui;

import com.atm.service.ATMService;
import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    JTextField accountField;
    JPasswordField pinField;

    public LoginFrame() {

        setTitle("ATM Login");
        setSize(450, 350);
        setLayout(new BorderLayout());

        // 🌈 BACKGROUND PANEL
        JPanel bgPanel = new JPanel(new GridBagLayout());
        bgPanel.setBackground(new Color(220, 230, 250));

        // 📦 CARD PANEL
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200), 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // 🧾 TITLE
        JLabel title = new JLabel("ATM Login");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        // 🔧 GRID SETTINGS
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 🔹 ACCOUNT LABEL
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel accLabel = new JLabel("Account No:");
        accLabel.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(accLabel, gbc);

        // 🔹 ACCOUNT FIELD
        gbc.gridx = 1;
        accountField = new JTextField();
        accountField.setPreferredSize(new Dimension(150,30));
        card.add(accountField, gbc);

        // 🔹 PIN LABEL
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel pinLabel = new JLabel("PIN:");
        pinLabel.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(pinLabel, gbc);

        // 🔹 PIN FIELD (FIXED SIZE)
        gbc.gridx = 1;
        pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(150,30));
        card.add(pinField, gbc);

        // 🔘 LOGIN BUTTON (VISIBLE + CENTERED)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(70,130,180));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(120,35));

        card.add(loginBtn, gbc);

        // 🧩 ADD TO MAIN
        bgPanel.add(card);

        add(title, BorderLayout.NORTH);
        add(bgPanel, BorderLayout.CENTER);

        // 🔥 LOGIN LOGIC
        loginBtn.addActionListener(e -> {
            String acc = accountField.getText();
            String pin = new String(pinField.getPassword());

            int userId = ATMService.login(acc, pin);

            if (userId != -1) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                new ATMFrame(userId);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!");
            }
        });

        // 🔥 ENTER KEY LOGIN SUPPORT
        getRootPane().setDefaultButton(loginBtn);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}