package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class MainMenu {

    PlayerMaster master;
    String roomList;

    JFrame frame;
    JTabbedPane tabbedPane;
    public int colourSelection = 0;
    public int spriteSelection = 0;
    boolean waiting = false;

    public MainMenu(PlayerMaster master, String roomList) {
        if (roomList.charAt(0) == 'Y') {
            JOptionPane.showConfirmDialog(frame,
                    "Incorrect Password","",
                    JOptionPane.DEFAULT_OPTION);

        }
        this.roomList = roomList.substring(1);
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
        waitingPanel.setBackground(Color.gray);
        JLabel colourLabel = new JLabel("Waiting for opponent");
        colourLabel.setHorizontalAlignment(JLabel.CENTER);
        colourLabel.setBounds(0,0,150,64);
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
        panel.add(Box.createVerticalStrut(15)); // Add some vertical spacing
        JButton Refresh = createButton("Refresh rooms", -1, false, "NULL");
        panel.add(Refresh);

        createNewRoom.setVisible(true);



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
                waitingWindow();
            }
        });
        return returnButton;
    }


    public class SettingsTab extends JPanel {

        private final JPanel gridSquareOne;
        private final JPanel gridSquareTwo;
        private final JLabel spriteOne;
        private final JLabel spriteTwo;


        public SettingsTab() {
            this.setBackground(Color.gray);
            readFile();
            setLayout(null); // Set layout manager to null for absolute positioning
            String[] colourFormats = {"Monochrome", "Pink", "Evil", "Ugly", "New"};
            String[] spriteFormats = {"Default", "Norights", "ComedyIsDead"};


            JLabel colourLabel = new JLabel("Select Grid Colours");
            colourLabel.setHorizontalAlignment(JLabel.CENTER);
            colourLabel.setBounds(40,10,180,40);

            JComboBox<String> colourSelectionBox = new JComboBox<>(colourFormats);
            colourSelectionBox.setSelectedIndex(colourSelection);
            colourSelectionBox.setBounds(40,40,200,25);
            colourSelectionBox.addActionListener(e -> updateSquareColors(colourSelectionBox.getSelectedIndex()));

            JLabel spriteLabel = new JLabel("Select Sprite Sheet");
            spriteLabel.setBounds(40,90,200,40);
            spriteLabel.setHorizontalAlignment(JLabel.CENTER);

            JComboBox<String> spriteSelectionBox = new JComboBox<>(spriteFormats);
            spriteSelectionBox.setBounds(40,120,200,25);
            spriteSelectionBox.setSelectedIndex(spriteSelection);
            spriteSelectionBox.addActionListener(e -> updateSpriteSheet(spriteSelectionBox.getSelectedIndex()));


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
            File file = new File("preferences.txt");
            try{
                if (!file.exists()) {
                    // If the file doesn't exist, create it
                    if (file.createNewFile()) {throw new RuntimeException("Couldnt create file");}
                    // Write to the file
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    String s = colourSelection + "\n" + spriteSelection;
                    writer.write(s);

                    writer.close();
                } else {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    colourSelection = Integer.parseInt(br.readLine());
                    spriteSelection = Integer.parseInt(br.readLine());
                    br.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        private void updateFile() {
            File file = new File("preferences.txt");
            try{
                if (!file.exists()) {
                    if (file.createNewFile()) {throw new RuntimeException("Couldnt create file");}
                    // Write to the file
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    String s = colourSelection + "\n" + spriteSelection;
                    writer.write(s);
                    writer.close();
                }
                FileWriter writer = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                String s = colourSelection + "\n" + spriteSelection;
                bufferedWriter.write(s);
                bufferedWriter.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        private void updateSquareColors(int selectedIndex) {
            colourSelection = selectedIndex;
            switch (selectedIndex) {
                case 0 -> { // Monochrome
                    gridSquareOne.setBackground(Color.BLACK);
                    gridSquareTwo.setBackground(Color.WHITE);
                }
                case 1 -> { // Pink
                    gridSquareOne.setBackground(Color.PINK);
                    gridSquareTwo.setBackground(Color.MAGENTA);
                }
                case 2 -> { // Evil
                    gridSquareOne.setBackground(Color.BLACK);
                    gridSquareTwo.setBackground(Color.RED);
                }
                case 3 -> { // Ugly
                    gridSquareOne.setBackground(Color.GREEN);
                    gridSquareTwo.setBackground(Color.YELLOW);
                }
                case 4 -> { // New
                    gridSquareOne.setBackground(Color.BLUE);
                    gridSquareTwo.setBackground(Color.ORANGE);
                }
                default -> {
                }
            }
            updateFile();
        }

        private void updateSpriteSheet(int selectedIndex) {
            spriteSelection = selectedIndex;
            String fileString = switch (selectedIndex) {
                case 1 -> "chess.png";
                case 2-> "chess3.png";
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

            spriteOne.setIcon(sprite1);
            spriteTwo.setIcon(sprite2);
            updateFile();
        }
    }
}