package com.example;

import javax.swing.*;

public class HelloSwing {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing Test");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("Hello, Swing!", SwingConstants.CENTER);
        frame.add(label);
        frame.setVisible(true);
    }
}