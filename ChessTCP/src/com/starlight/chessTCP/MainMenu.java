package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;


public class MainMenu {

    UIHandler master;
    String roomList;

    JFrame frame;
    JTabbedPane tabbedPane;
    public int colourSelection = 0;
    public int spriteSelection = 0;
    boolean waiting = false;

    public MainMenu(UIHandler master, String roomList) {
        if (roomList.startsWith("RETRY")) {
            JOptionPane.showConfirmDialog(frame,
                    "Incorrect Password","",
                    JOptionPane.DEFAULT_OPTION);

        }
        this.roomList = roomList.substring(5);      //Removes the first notifier (FIRST/RETRY)
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
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Rooms", createRoomTab());
        tabbedPane.addTab("Settings", new SettingsTab());
        frame.add(tabbedPane);
        return frame;
    }
    public void waitingWindow() {
        waiting = true;
        frame.remove(tabbedPane);
        frame.setBounds(200, 100, 300, 400);
        frame.revalidate();
        frame.repaint();
        JPanel waitingPanel = new JPanel();
        waitingPanel.setBackground(Color.white);
        JLabel colourLabel = new JLabel("Waiting for opponent");
        colourLabel.setHorizontalAlignment(JLabel.CENTER);
        colourLabel.setBounds(0,0,150,64);
        colourLabel.setForeground(Color.CYAN);
        colourLabel.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        waitingPanel.add(colourLabel);


        frame.revalidate();
        frame.add(waitingPanel);
        //frame.repaint();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Quit waiting?", "Quit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    master.closeGame(false);
                    System.exit(0);
                }
            }
        });
    }
    public JScrollPane createRoomTab() {
            JPanel panel = new JPanel();
            panel.setBackground(Color.white);
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

            if (!roomList.isEmpty()) {
                for (int i = 0; i < roomList.length() / 6; i++) {
                    int players = roomList.charAt(i * 6) - 48;
                    int spectators = roomList.charAt((i * 6) + 1)- 48;
                    boolean requiresPassword = roomList.substring(((i * 6) + 2), (i * 6) + 6).equals("LOCK");
                    JButton newButton = createButton(players + "Players - " + spectators + " watching - " +
                            (requiresPassword? "Locked" : "Open"), i + 2, requiresPassword, "Enter password");
                    panel.add(newButton);
                    panel.add(Box.createVerticalStrut(15)); // Add some vertical spacing
                }
            }
        panel.add(Box.createVerticalStrut(15)); // Add some vertical spacing
        JButton Refresh = createButton("Refresh rooms", -1, false, "NULL");
        panel.add(Refresh);

        createNewRoom.setVisible(true);



            return scrollPane;
        }

    private JButton createButton(String text, int returnVal, boolean locked, String dialogueText) {
        JButton returnButton = new JButton(text);
        returnButton.setBorder(BorderFactory.createSoftBevelBorder(0));
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setBackground(Color.CYAN);
        returnButton.setForeground(Color.BLACK);
        returnButton.setFocusable(false);
        returnButton.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = "";
                if (locked) {
                    password = JOptionPane.showInputDialog(dialogueText);
                }
                master.sendRoom(returnVal, password);
                waitingWindow();
            }
        });
        return returnButton;
    }

    public void notifyShutdown() {
        JOptionPane.showConfirmDialog(frame,
                ("The server has closed, please exit."), "Quit.",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }


    public class SettingsTab extends JPanel {

        private final JPanel gridSquareOne;
        private final JPanel gridSquareTwo;
        private final JLabel spriteOne;
        private final JLabel spriteTwo;


        public SettingsTab() {
            this.setBackground(Color.white);
            readFile();
            setLayout(null); // Set layout manager to null for absolute positioning
            String[] colourFormats = {"Monochrome", "Pink", "Swampy"};
            String[] spriteFormats = {"Default", "ComedyIsDead"};


            JLabel colourLabel = new JLabel("Select Grid Colours");
            colourLabel.setHorizontalAlignment(JLabel.CENTER);
            colourLabel.setBounds(40,10,200,40);
            colourLabel.setFont(new Font(Font.SERIF, Font.BOLD, 16));
            colourLabel.setForeground(Color.BLACK);

            JComboBox<String> colourSelectionBox = new JComboBox<>(colourFormats);
            colourSelectionBox.setSelectedIndex(colourSelection);
            colourSelectionBox.setBounds(40,40,200,25);
            colourSelectionBox.addActionListener(e -> updateSquareColors(colourSelectionBox.getSelectedIndex()));
            colourSelectionBox.setForeground(Color.BLACK);
            colourSelectionBox.setBackground(Color.CYAN);


            JLabel spriteLabel = new JLabel("Select Sprite Sheet");
            spriteLabel.setBounds(40,90,200,40);
            spriteLabel.setHorizontalAlignment(JLabel.CENTER);
            spriteLabel.setFont(new Font(Font.SERIF, Font.BOLD, 16));
            spriteLabel.setForeground(Color.BLACK);

            JComboBox<String> spriteSelectionBox = new JComboBox<>(spriteFormats);
            spriteSelectionBox.setBounds(40,120,200,25);
            spriteSelectionBox.setSelectedIndex(spriteSelection);
            spriteSelectionBox.addActionListener(e -> updateSpriteSheet(spriteSelectionBox.getSelectedIndex()));
            spriteSelectionBox.setForeground(Color.BLACK);
            spriteSelectionBox.setBackground(Color.CYAN);


            gridSquareOne = new JPanel();
            gridSquareOne.setPreferredSize(new Dimension(50, 50));
            gridSquareOne.setBounds(40,220,80,80);


            gridSquareTwo = new JPanel();
            gridSquareTwo.setPreferredSize(new Dimension(50, 50));
            gridSquareTwo.setBounds(160,220,80,80);
            updateSquareColors(colourSelection); // Initially set colors based on the first selection

            spriteOne = new JLabel();
            spriteOne.setBounds(40,220,80,80);

            spriteTwo = new JLabel();
            spriteTwo.setBounds(160,220,80,80);
            updateSpriteSheet(spriteSelection);






            this.add(colourLabel);
            this.add(colourSelectionBox);
            this.add(spriteSelectionBox);
            this.add(spriteOne);
            this.add(spriteTwo);
            this.add(gridSquareOne);
            this.add(gridSquareTwo);
            this.add(spriteLabel);


        }

        private void readFile() {
            String FILE_PATH = "/preferences.txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                colourSelection = Integer.parseInt(reader.readLine());
                spriteSelection = Integer.parseInt(reader.readLine());
                System.out.println(colourSelection);
                System.out.println(spriteSelection);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void updateFile() {
            String FILE_PATH = "/preferences.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
                writer.write(String.valueOf(colourSelection));
                writer.newLine();
                writer.write(String.valueOf(spriteSelection));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void updateSquareColors(int selectedIndex) {
            colourSelection = selectedIndex;
            switch (selectedIndex) {

                case 1 -> { // Pink
                    gridSquareOne.setBackground(Color.PINK);
                    gridSquareTwo.setBackground(Color.MAGENTA);
                }
                case 2 -> { // Swampy
                    gridSquareOne.setBackground(new Color(55, 234, 55));
                    gridSquareTwo.setBackground(new Color(62, 75, 3));
                }

                default -> { // Monochrome
                    gridSquareOne.setBackground(new Color(65, 64, 64));
                    gridSquareTwo.setBackground(Color.WHITE);
                }
            }
            updateFile();
        }

        private void updateSpriteSheet(int selectedIndex) {
            spriteSelection = selectedIndex;
            String fileString = switch (selectedIndex) {
                case 1 -> "/chess3.png";
                default -> "/chess2.png";
            };

            System.out.println(fileString);
            //gets sprite sheet
            BufferedImage baseImage;
            InputStream inputStream = getClass().getResourceAsStream(fileString);
            try {
                baseImage = ImageIO.read(inputStream);
            } catch (Exception e) {
                System.out.println("Couldn't find image");
                throw new RuntimeException(e);
            }

            int width = baseImage.getWidth();
            int height = baseImage.getHeight();

            ImageIcon sprite1 =  new ImageIcon(baseImage.getSubimage(3 * width/6 ,
                    0, width/6, height/2).getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH));
            ImageIcon sprite2 =  new ImageIcon(baseImage.getSubimage(0,
                    height/2, width/6, height/2).getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH));

            spriteOne.setIcon(sprite1);
            spriteTwo.setIcon(sprite2);
            updateFile();
        }
    }
}