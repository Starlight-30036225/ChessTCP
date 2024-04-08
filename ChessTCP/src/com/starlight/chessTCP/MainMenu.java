package com.starlight.chessTCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class MainMenu {

    PlayerMaster master;
    String roomList;
    
    JFrame frame;

    public MainMenu(PlayerMaster master, String roomList) {
        this.roomList = roomList;
        this.master = master;
        frame = getFrame();
        paintFrame();
        frame.setVisible(true);

    }


    private JFrame getFrame() {
        JFrame frame = new JFrame();
        frame.setBounds(200, 100, 300, 500);
        frame.setUndecorated(false);
        GridLayout griddy = new GridLayout();
        frame.setLayout(griddy);
        frame.setName("CHESS");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.LIGHT_GRAY);
        return frame;
    }
    private void paintFrame() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.gray);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Setting BoxLayout to vertically align components

        // Create a vertical scrollbar
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Create the "Create New Room" button
        JButton createNewRoom = createButton("Create New Room", 1, true, "Enter password, leave black for no password:");
        panel.add(createNewRoom);
        createNewRoom.setVisible(true);
        panel.add(Box.createVerticalStrut(15)); // Add some vertical spacing
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        if (!roomList.equals("...")) {
            for (int i = 0; i < roomList.length() / 3; i++) {
                boolean requiresPassword = (roomList.charAt((i * 3) + 2) == 'Y');
                JButton newButton = createButton((i + 2) + ": " + roomList.charAt(i * 3) + " Connected - " +
                        (requiresPassword? "Locked" : "Open"), i + 2, requiresPassword, "Enter password");
                panel.add(newButton);
                panel.add(Box.createVerticalStrut(15)); // Add some vertical spacing
            }
        }

        frame.add(BorderLayout.CENTER, scrollPane);
        frame.setVisible(true);
    }


    private JButton createButton(String text, int returnval, boolean locked, String dialogueText) {
        JButton returnButton = new JButton(text);
        returnButton.setBorder(BorderFactory.createSoftBevelBorder(0));
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setBackground(Color.MAGENTA);
        returnButton.setForeground(Color.GREEN);
        returnButton.setFocusable(false);
        returnButton.setPreferredSize(new Dimension(180,300));
        returnButton.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = "";
                if (locked) {
                    password = JOptionPane.showInputDialog(dialogueText);
                }
                master.sendRoom(returnval, password);
                frame.dispose();
            }
        });
        return returnButton;
    }
}