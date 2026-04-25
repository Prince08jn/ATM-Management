package com.atm.service;

import com.atm.db.DBConnection;
import java.sql.*;

public class ATMService {

    // LOGIN METHOD
  public static int login(String acc, String pin) {
    int userId = -1;

    try {
        Connection con = DBConnection.getConnection();

        String query = "SELECT id FROM users WHERE account_number=? AND pin=?";
        PreparedStatement ps = con.prepareStatement(query);

        // 🔥 CLEAN INPUT (VERY IMPORTANT)
        acc = acc.trim();
        pin = pin.trim();

        System.out.println("ACC: " + acc);
        System.out.println("PIN: " + pin);

        ps.setString(1, acc);
        ps.setInt(2, Integer.parseInt(pin));

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            userId = rs.getInt("id");
            System.out.println("✅ LOGIN SUCCESS");
        } else {
            System.out.println("❌ LOGIN FAILED");
        }

        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return userId;
}

    // GET BALANCE
    public int getBalance(int userId) {
        int balance = 0;

        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT balance FROM users WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                balance = rs.getInt("balance");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balance;
    }

    // DEPOSIT
    public void deposit(int userId, int amount) {
    try {
        Connection con = DBConnection.getConnection();

        // update balance
        String query = "UPDATE users SET balance = balance + ? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, amount);
        ps.setInt(2, userId);
        ps.executeUpdate();

        // 🔥 transaction insert
        String tQuery = "INSERT INTO transactions(user_id, type, amount) VALUES (?, 'DEPOSIT', ?)";
        PreparedStatement tps = con.prepareStatement(tQuery);
        tps.setInt(1, userId);
        tps.setInt(2, amount);
        tps.executeUpdate();

        System.out.println("Deposit inserted in DB ✅"); // DEBUG

        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // WITHDRAW (FINAL 🔥)
   public boolean withdraw(int userId, int amount) {
    try {
        Connection con = DBConnection.getConnection();

        // check balance
        String checkQuery = "SELECT balance FROM users WHERE id=?";
        PreparedStatement cps = con.prepareStatement(checkQuery);
        cps.setInt(1, userId);
        ResultSet rs = cps.executeQuery();

        if (rs.next()) {
            int balance = rs.getInt("balance");

            if (balance >= amount) {

                // update balance
                String query = "UPDATE users SET balance = balance - ? WHERE id=?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();

                // 🔥 VERY IMPORTANT (missing tha)
                String tQuery = "INSERT INTO transactions(user_id, type, amount) VALUES (?, 'WITHDRAW', ?)";
                PreparedStatement tps = con.prepareStatement(tQuery);
                tps.setInt(1, userId);
                tps.setInt(2, amount);
                tps.executeUpdate();

                System.out.println("Withdraw inserted in DB ✅");

                con.close();
                return true;
            }
        }

        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}

    public String getName(int userId) {
    String name = "";

    try {
        Connection con = DBConnection.getConnection();

        String query = "SELECT name FROM users WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            name = rs.getString("name");
        }

        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return name;
}

public String getTransactions(int userId) {
    String data = "";

    try {
        Connection con = DBConnection.getConnection();

        String query = "SELECT type, amount, date FROM transactions WHERE user_id=?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            data += rs.getString("type") + " - ₹"
                    + rs.getInt("amount") + " (" 
                    + rs.getString("date") + ")\n";
        }

        con.close(); // 🔥 important

    } catch (Exception e) {
        e.printStackTrace();
    }

    return data;
}
public void changePin(int userId, String newPin) {
    try {
        Connection con = DBConnection.getConnection();

        String query = "UPDATE users SET pin=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, newPin);
        ps.setInt(2, userId);

        ps.executeUpdate();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public boolean transfer(int senderId, String receiverAcc, int amount) {
    try {
        Connection con = DBConnection.getConnection();

        // 🔍 find receiver
        String findQuery = "SELECT id FROM users WHERE account_number=?";
        PreparedStatement fps = con.prepareStatement(findQuery);
        fps.setString(1, receiverAcc);
        ResultSet rs = fps.executeQuery();

        if (rs.next()) {
            int receiverId = rs.getInt("id");

            int balance = getBalance(senderId);

            if (balance >= amount) {

                // ➖ deduct sender
                PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE users SET balance = balance - ? WHERE id=?");
                ps1.setInt(1, amount);
                ps1.setInt(2, senderId);
                ps1.executeUpdate();

                // ➕ add receiver
                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE users SET balance = balance + ? WHERE id=?");
                ps2.setInt(1, amount);
                ps2.setInt(2, receiverId);
                ps2.executeUpdate();

                // 📝 transaction entry
                PreparedStatement tps = con.prepareStatement(
                    "INSERT INTO transactions(user_id, receiver_id, type, amount) VALUES (?, ?, 'TRANSFER', ?)");
                tps.setInt(1, senderId);
                tps.setInt(2, receiverId);
                tps.setInt(3, amount);
                tps.executeUpdate();

                // 📝 sender entry
                PreparedStatement tps1 = con.prepareStatement(
               "INSERT INTO transactions(user_id, receiver_id, type, amount) VALUES (?, ?, 'TRANSFER', ?)");
               tps1.setInt(1, senderId);
               tps1.setInt(2, receiverId);
               tps1.setInt(3, amount);
               tps1.executeUpdate();

               // 📝 receiver entry (🔥 NEW)
               PreparedStatement tps2 = con.prepareStatement(
               "INSERT INTO transactions(user_id, receiver_id, type, amount) VALUES (?, ?, 'RECEIVE', ?)");
               tps2.setInt(1, receiverId);
                tps2.setInt(2, senderId);
                tps2.setInt(3, amount);
                 tps2.executeUpdate();

                con.close();
                return true;
            }
        }

        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return false;
}
}