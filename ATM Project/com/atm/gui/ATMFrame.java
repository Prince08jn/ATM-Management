package com.atm.gui;

import com.atm.service.ATMService;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class ATMFrame extends JFrame {

    int userId;

    // ✅ BUTTON STYLE METHOD (OUTSIDE CONSTRUCTOR)
   private JButton createButton(String text) {
    JButton btn = new JButton(text);

    btn.setFocusPainted(false);
    btn.setFont(new Font("Arial", Font.BOLD, 14));

   btn.setBackground(new Color(52,152,219)); // nicer blue
    btn.setForeground(Color.WHITE);

    btn.setOpaque(true);             
    btn.setBorderPainted(false);     

    btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

    return btn;
}

    public ATMFrame(int userId) {
        this.userId = userId;

        setTitle("ATM Dashboard");
        setSize(350, 450);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(8,1,10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        mainPanel.setBackground(new Color(240,240,240));

        ATMService service = new ATMService();

        // 👤 USER NAME
        String name = service.getName(userId);

        JLabel welcomeLabel = new JLabel("Welcome, " + name + " 👋");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton balanceBtn = createButton("Check Balance");
        JButton depositBtn = createButton("Deposit");
        JButton withdrawBtn = createButton("Withdraw");
        JButton transferBtn = createButton("Transfer Money");
        JButton miniBtn = createButton("Mini Statement");
        JButton pinBtn = createButton("Change PIN");
        JButton logoutBtn = createButton("Logout");

        // 🔹 BALANCE
        balanceBtn.addActionListener(e -> {
            int balance = service.getBalance(userId);
            JOptionPane.showMessageDialog(this, "Balance: ₹" + balance);
        });

        // 🔹 DEPOSIT
        depositBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter amount:");

            try {
                int amount = Integer.parseInt(input);

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter valid amount!");
                    return;
                }

                service.deposit(userId, amount);
                int balance = service.getBalance(userId);

                JOptionPane.showMessageDialog(this,
                        "Deposited Successfully!\nNew Balance: ₹" + balance);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });

        // 🔹 WITHDRAW
        withdrawBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter amount:");

            try {
                int amount = Integer.parseInt(input);

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter valid amount!");
                    return;
                }

                if (service.withdraw(userId, amount)) {
                    int balance = service.getBalance(userId);

                    JOptionPane.showMessageDialog(this,
                            "Withdraw Successful!\nRemaining Balance: ₹" + balance);
                } else {
                    JOptionPane.showMessageDialog(this, "Insufficient Balance!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });

        // 🔥 MINI STATEMENT
        miniBtn.addActionListener(e -> {
            try {
                Connection con = com.atm.db.DBConnection.getConnection();

                String query = "SELECT t.type, t.amount, t.date, u1.name AS sender, u2.name AS receiver " +
                        "FROM transactions t " +
                        "LEFT JOIN users u1 ON t.user_id = u1.id " +
                        "LEFT JOIN users u2 ON t.receiver_id = u2.id " +
                        "WHERE t.user_id=? ORDER BY t.date DESC";

                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, userId);

                ResultSet rs = ps.executeQuery();

                String[] columnNames = {"Type", "Amount", "Details", "Date"};
                java.util.ArrayList<Object[]> dataList = new java.util.ArrayList<>();

                while (rs.next()) {
                    String type = rs.getString("type");
                    int amount = rs.getInt("amount");
                    String date = rs.getString("date");

                    String sender = rs.getString("sender");
                    String receiver = rs.getString("receiver");

                    String details = "";

                    if (type.equals("TRANSFER")) {
                        details = "→ " + receiver;
                    } else if (type.equals("RECEIVE")) {
                        details = "← " + sender;
                    } else {
                        details = "-";
                    }

                    dataList.add(new Object[]{
                            type,
                            "₹" + amount,
                            details,
                            date
                    });
                }

                if (dataList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No Transactions Found!");
                    return;
                }

                Object[][] data = new Object[dataList.size()][4];
                for (int i = 0; i < dataList.size(); i++) {
                    data[i] = dataList.get(i);
                }

                JTable table = new JTable(data, columnNames);

                // 👇 yahi pe styling karni hai
               table.setRowHeight(28);
               table.setFont(new Font("Arial", Font.PLAIN, 14));
               table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
               table.getTableHeader().setBackground(new Color(70,130,180));
               table.getTableHeader().setForeground(Color.white);
               table.setSelectionBackground(new Color(200,220,255));

               JScrollPane scrollPane = new JScrollPane(table);

                JFrame frame = new JFrame("Mini Statement");
                frame.setSize(500, 300);
                frame.add(scrollPane);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                con.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading transactions!");
            }
        });

        // 🔹 TRANSFER
        transferBtn.addActionListener(e -> {
            String acc = JOptionPane.showInputDialog(this, "Receiver Account No:");
            String amt = JOptionPane.showInputDialog(this, "Enter amount:");

            try {
                int amount = Integer.parseInt(amt);

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid amount!");
                    return;
                }

                if (service.transfer(userId, acc, amount)) {
                    JOptionPane.showMessageDialog(this, "Transfer Successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Transfer Failed!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });

        // 🔹 CHANGE PIN
        pinBtn.addActionListener(e -> {
            String newPin = JOptionPane.showInputDialog(this, "Enter new PIN:");

            if (newPin != null && newPin.length() == 4) {
                service.changePin(userId, newPin);
                JOptionPane.showMessageDialog(this, "PIN Updated!");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN!");
            }
        });

        // 🔹 LOGOUT
        logoutBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        // 🔥 ADD TO PANEL
        mainPanel.add(welcomeLabel);
        mainPanel.add(balanceBtn);
        mainPanel.add(depositBtn);
        mainPanel.add(withdrawBtn);
        mainPanel.add(transferBtn);
        mainPanel.add(miniBtn);
        mainPanel.add(pinBtn);
        mainPanel.add(logoutBtn);

        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}