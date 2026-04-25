package com.atm.model;

public class User {
    private String accountNumber;
    private int pin;
    private double balance;

    public User(String accountNumber, int pin, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public int getPin() { return pin; }
    public double getBalance() { return balance; }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}