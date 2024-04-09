package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainMenu {

    PlayerMaster master;
    String roomList;

    JFrame frame;

    public MainMenu(PlayerMaster master, String roomList) {
        this.roomList = roomList;
        this.master = master;
        frame = getFrame();
        //paintFrame();
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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Rooms", createRoomTab());
        tabbedPane.addTab("Settings", new SettingsTab());
        frame.add(tabbedPane);
        return frame;
    }

        public JScrollPane createRoomTab() {
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
            return scrollPane;
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

    public class SettingsTab extends JPanel {

        private final JPanel square1;
        private final JPanel square2;

        private final JLabel picLabel;
        private final JLabel picLabel2;

        public SettingsTab() {
            setLayout(null); // Set layout manager to null for absolute positioning
            String[] colourFormats = {"Monochrome", "Pink", "Evil", "Ugly", "New"};
            String[] spriteFormats = {"one", "two"};
            JLabel label = new JLabel("Select Grid Colours");
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setBounds(40,10,180,40);

            JComboBox<String> selectionBox = new JComboBox<>(colourFormats);
            selectionBox.setBounds(40,40,200,25);
            selectionBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateSquareColors(selectionBox.getSelectedIndex());
                }
            });

            JLabel label2 = new JLabel("Select Sprite Sheet");
            label2.setBounds(40,90,200,40);
            label2.setHorizontalAlignment(JLabel.CENTER);

            JComboBox<String> selectionBox2 = new JComboBox<>(spriteFormats);
            selectionBox2.setBounds(40,120,200,25);
            selectionBox2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateSpriteSheet(selectionBox2.getSelectedIndex());
                }
            });

            square1 = new JPanel();
            square2 = new JPanel();

            square1.setPreferredSize(new Dimension(50, 50));
            square1.setBounds(40,220,80,80);
            square2.setPreferredSize(new Dimension(50, 50));
            square2.setBounds(160,220,80,80);
            updateSquareColors(0); // Initially set colors based on the first selection





            picLabel = new JLabel();
            picLabel2 = new JLabel();
            updateSpriteSheet(0);

            picLabel.setBounds(40,220,80,80);
            picLabel2.setBounds(160,220,80,80);


            this.add(label);
            this.add(selectionBox);
            this.add(selectionBox2);
            this.add(picLabel);
            this.add(picLabel2);
            this.add(square1);
            this.add(square2);
            this.add(label2);


        }

        private void updateSquareColors(int selectedIndex) {
            switch (selectedIndex) {
                case 0: // Monochrome
                    square1.setBackground(Color.BLACK);
                    square2.setBackground(Color.WHITE);
                    break;
                case 1: // Pink
                    square1.setBackground(Color.PINK);
                    square2.setBackground(Color.WHITE);
                    break;
                case 2: // Evil
                    square1.setBackground(Color.RED);
                    square2.setBackground(Color.BLACK);
                    break;
                case 3: // Ugly
                    square1.setBackground(Color.GREEN);
                    square2.setBackground(Color.YELLOW);
                    break;
                case 4: // New
                    square1.setBackground(Color.BLUE);
                    square2.setBackground(Color.ORANGE);
                    break;
                default:
                    break;
            }
        }

        private void updateSpriteSheet(int selectedIndex) {
            String fileString = switch (selectedIndex) {
                case 1 -> "chess.png";
                default -> "chess2.png";
            };


            //gets sprite sheet
            BufferedImage baseImage;
            try {
                baseImage = ImageIO.read(new File("src/Resources/" + fileString));
            } catch (IOException e) {
                System.out.println("Couldn't find image");
                throw new RuntimeException(e);
            }

            int width = baseImage.getWidth();
            int height = baseImage.getHeight();

            ImageIcon sprite1 =  new ImageIcon(baseImage.getSubimage(3 * width/6 ,
                    0, width/6, height/2).getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH));
            ImageIcon sprite2 =  new ImageIcon(baseImage.getSubimage(0,
                    height/2, width/6, height/2).getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH));

            picLabel.setIcon(sprite1);
            picLabel2.setIcon(sprite2);
        }
    }
}