package core;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

import operations.commands.InvalidCommandException;
import operations.commands.PresenceCommand;
import operations.commands.RoomMessageCommand;
import operations.commands.UserMessageCommand;
import packets.ChatPacket;
import transport.UdpMulticast;
import util.Configuration;
import util.LongInteger;
import core.ChatPacketCallback;
import core.ChatSocket;

//the public class for the chat gi
public class ChatGUI extends JFrame {

    private static final long serialVersionUID = -8020776555862630725L;
    // private data types used by the gui
    private JPanel contentPane;
    private JTextField textField;
    private JTabbedPane tabbedPane;
    private JTextField textField_3;
    private static ChatSocket sock;

    /**
     * The addTab class add a tab and facilitates several actions from
     * components within the tab
     */
    // The add tab function will add a tab to the tabbed pane
    public void addTab(final String name, String[] roomMembers, final int roomOrUser) {

        // initialize panel for the pane
        JPanel panel_1 = new JPanel();

        // add a tab to the tabbed pane
        tabbedPane.addTab(name, null, panel_1, null);

        // set the layout for the tab
        panel_1.setLayout(new BorderLayout(5, 0));

        // set the layou for the bottom that has send button and input message
        // textfield
        JPanel panel_2 = new JPanel();
        panel_1.add(panel_2, BorderLayout.SOUTH);
        panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // create text area for input text message and make it scrollable
        final JTextArea textArea_1 = new JTextArea(3, 100);
        JScrollPane scrollPane3 = new JScrollPane(textArea_1);
        panel_2.add(scrollPane3);

        // create the text are for display of text entered by user and received
        // from othes
        final JTextArea textArea_2 = new JTextArea();

        // create the send button that sends text
        JButton btnNewButton_2 = new JButton("SEND");
        // the action listener perform an action in relation to the button click
        btnNewButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // send the text to the room in relation to the chat
                // using RoomName or user to which message is destined
                try {
                    if (roomOrUser == 1) {
                        // send message to a room
                        sock.executeCommand(new RoomMessageCommand(new LongInteger(name), textArea_1.getText()
                                .getBytes()));
                        textArea_2.setText(textArea_2.getText() + textField.getText() + ":  " + textArea_1.getText()
                                + "\n");
                    } else if (roomOrUser == 2) {
                        // send message to a user
                        sock.executeCommand(new UserMessageCommand(new LongInteger(name), textArea_1.getText()
                                .getBytes(), true));
                        textArea_2.setText(textArea_2.getText() + textField.getText() + ":  " + textArea_1.getText()
                                + "\n");
                    }
                } catch (InvalidCommandException ex) {
                    JOptionPane.showMessageDialog(null, "Unable to join or leave room.", "alert",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel_2.add(btnNewButton_2);

        // make the display text area non editable and scrollable
        JScrollPane scrollPane = new JScrollPane(textArea_2);
        textArea_2.setEditable(false);
        panel_1.add(scrollPane, BorderLayout.CENTER);
        textArea_2.setColumns(10);

        // set room members to list column
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < roomMembers.length; i++)
            listModel.addElement(roomMembers[i]);
        JList list = new JList(listModel);
        // make the list section scrollable
        JScrollPane scrollPane2 = new JScrollPane(list);
        panel_1.add(scrollPane2, BorderLayout.EAST);

        // ////need to refresh the list with room users check intermittently,
        // timer function

        // create panel and layout that has exit button and label for each tab
        JPanel panel_3 = new JPanel();
        panel_1.add(panel_3, BorderLayout.NORTH);
        panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // create the exit label and button
        JLabel lblNewLabel_1 = new JLabel("Click on the Button to your right to close the tab pane");
        panel_3.add(lblNewLabel_1);
        JButton btnNewButton_3 = new JButton("X");
        // add a action listener that removes tab on button click
        btnNewButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int n = JOptionPane.showConfirmDialog(contentPane, "Are you sure you want to close this tab titled "
                        + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()) + " ?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (n == 0)
                    removeTab();
            }
        });
        btnNewButton_3.setHorizontalAlignment(SwingConstants.LEFT);
        panel_3.add(btnNewButton_3);
    }

    /**
     * The removeTab class removes the tab selected in conjunction with its call
     * in the class above
     */
    // The remove tab function will remove a tab to the tabbed pane
    public void removeTab() {
        // confirm selected tab and remove it
        tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
        // if all tabs are removed
        if (tabbedPane.getTabCount() == 0) {
            // re-enable the username textfield and issue prompt notifying user
            JOptionPane.showMessageDialog(null, "Username textField re-enabled", "alert", JOptionPane.ERROR_MESSAGE);
            textField.setEnabled(true);
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // invoke the queue for the main
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // create an instance of the chat gui frame
                    ChatGUI frame = new ChatGUI();
                    // make the frame visible
                    frame.setVisible(true);
                } catch (Exception e) {
                    // print stack trace to terminal
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    // below is the chat gui frame which houses the fields that facilitate a
    // connection to the chat socket with field components within
    public ChatGUI() {

        String propertiesPath = "kchat.properties";
        try {
            Configuration.getInstance().setFile(propertiesPath);
            sock = new ChatSocket(new UdpMulticast(Configuration.getInstance().getValueAsString("udp.iface"),
                    Configuration.getInstance().getValueAsString("udp.host"), Configuration.getInstance()
                            .getValueAsInt("udp.port")), new ChatPacketCallback() {

                @Override
                public void receivePacket(ChatPacket message) {
                    System.out.println("! Received Packet " + message);
                    System.out.println("----------------------------");
                }
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot Open Socket", "alert", JOptionPane.ERROR_MESSAGE);
            // Exit the application if socket is not established
            // print stack trace to terminal
            e.printStackTrace();
            System.exit(0);
        }

        // exit the frame on closure of the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // set the bounds of the frame
        setBounds(100, 100, 450, 300);

        // Initialize the content pane
        contentPane = new JPanel();

        // set the borders of the content pane
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        // set the layout of the content pane
        contentPane.setLayout(new BorderLayout(0, 0));

        // add content pane to frame
        setContentPane(contentPane);

        // create panel and layout for input, button and field at top of content
        // pane
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        contentPane.add(panel, BorderLayout.NORTH);

        // create the username lael
        JLabel lblNewLabel = new JLabel("Username");
        panel.add(lblNewLabel);

        // create the field for the Username to input text
        textField = new JTextField();
        panel.add(textField);
        // set column length of the field
        textField.setColumns(10);

        // get rooms from the socket return array of long
        LongInteger[] rooms = sock.getPresenceManager().getRooms();
        // initialize field to store room names
        String[] roomNames = new String[rooms.length];
        // using for loop convert long to string and store in room name array
        for (int i = 0; i < rooms.length; i++) {
            roomNames[i] = rooms[i].toString();
        }

        // create the combo box
        final JComboBox comboBox = new JComboBox(roomNames);

        // refresh the rooms on button press
        final JButton btnNewButton = new JButton("Refresh Rooms");
        // the action listener for button press
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                comboBox.removeAllItems();
                // get rooms from the socket
                LongInteger[] rooms = sock.getPresenceManager().getRooms();
                // initialize field to store room names
                String[] roomNames = new String[rooms.length];
                // using for loop convert long to string and store in room name
                // array
                for (int i = 0; i < rooms.length; i++) {
                    comboBox.addItem(roomNames);
                    roomNames[i] = rooms[i].toString();
                }
            }
        });

        // add the label for joins
        JLabel lblJoin = new JLabel(" [--- joins ");
        panel.add(lblJoin);

        // add the textfield for user input for user to user connection
        textField_3 = new JTextField();

        // create a checkbox for when you which to select drop down or input
        // user
        final JCheckBox chckbxNewCheckBox = new JCheckBox("");
        chckbxNewCheckBox.setSelected(true);
        final JCheckBox chckbxNewCheckBox_1 = new JCheckBox("");

        // add action listener to checkbox for drop down
        chckbxNewCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // if checkbox is checked
                if (chckbxNewCheckBox.isSelected() == true) {
                    // set the other checkbox to unchecked
                    chckbxNewCheckBox_1.setSelected(false);
                    // enable the combox box drop down
                    comboBox.setEnabled(true);
                    // disable the textfield for the user input
                    textField_3.setEnabled(false);
                    textField_3.setText("");
                    // enable the refresh rooms button
                    btnNewButton.setEnabled(true);
                }
            }
        });
        panel.add(chckbxNewCheckBox);

        // add the drop down to the contentpane
        panel.add(comboBox);

        // additional labels are added to the content pane
        JLabel lblOrChatWith = new JLabel(" OR chat with user");
        panel.add(lblOrChatWith);

        // add action listener to checkbox for user input
        chckbxNewCheckBox_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // if checkbox is checked
                if (chckbxNewCheckBox_1.isSelected() == true) {
                    // set the other checkbox to unchecked
                    chckbxNewCheckBox.setSelected(false);
                    // disable the combox box drop down
                    comboBox.setEnabled(false);
                    // enable the textfield for the user input
                    textField_3.setEnabled(true);
                    textField_3.setText("");
                    // disable the refresh rooms button
                    btnNewButton.setEnabled(false);
                }
            }
        });
        panel.add(chckbxNewCheckBox_1);

        // set the textfield for user to user input to false by default and add
        // to contentpane panel
        textField_3.setEnabled(false);
        panel.add(textField_3);
        textField_3.setColumns(10);

        // the label is added to the content pane panel
        JLabel label = new JLabel(" ---] ");
        panel.add(label);

        // the refresh rooms button is added to content pane panel
        panel.add(btnNewButton);

        // the connection button is created
        JButton btnNewButton_1 = new JButton("Connect");
        // action listener is added to button to take action on key press
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // if no input or only spaces is put in text field it counts as
                // invalid entry
                if (textField.getText().trim().equals("")) {
                    // prompt to user and do nothing
                    JOptionPane.showMessageDialog(null, "Please input a valid username", "alert",
                            JOptionPane.ERROR_MESSAGE);
                    textField.setText("");
                } else {
                    // if checkbox for room drop down is selected meaning we are
                    // connecting to a room
                    if (chckbxNewCheckBox.isSelected() == true) {

                        // if no tab exist with a chosen room name continue
                        if (tabbedPane.indexOfTab(comboBox.getSelectedItem().toString()) == -1) {

                            // get the room members
                            Set<LongInteger> roomMembers = sock.getPresenceManager().membersOf(
                                    new LongInteger(comboBox.getSelectedItem().toString()));
                            // initialize field to store room member names
                            String[] roomMemberNames = new String[roomMembers.size()];
                            // using for loop convert long to string and store
                            // in room member name array
                            int i = 0;
                            for (LongInteger member : roomMembers) {
                                roomMemberNames[i] = member.toString();
                                i++;
                            }

                            // connect to room below and based on return type
                            // add tab
                            try {
                                sock.executeCommand(new PresenceCommand(new LongInteger(comboBox.getSelectedItem()
                                        .toString()), true));
                            } catch (InvalidCommandException ex) {
                                JOptionPane.showMessageDialog(null, "Unable to join room.", "alert",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            // add tab based on return type above
                            // once you connect successfully disable the
                            // username textfield
                            textField.setEnabled(false);
                            // add tab for Room to user chat
                            addTab(comboBox.getSelectedItem().toString(), roomMemberNames, 1);
                        } else {
                            // prompt user and ensures that one does not open
                            // multiple tabs to the same room
                            JOptionPane.showMessageDialog(null, "You are already connected to "
                                    + comboBox.getSelectedItem().toString(), "alert", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    // if checkbox for user is selected meaning we are
                    // connecting to a user
                    else if (chckbxNewCheckBox_1.isSelected() == true) {

                        if (tabbedPane.indexOfTab("Chat with " + textField_3.getText()) == -1) {
                            // one does not connect to a user so the tab is
                            // automatically created
                            // once you connect successfully disable the
                            // username textfield
                            // disable the textfield for usernname
                            textField.setEnabled(false);
                            // add tab for User to user chat
                            addTab("Chat with " + textField_3.getText(), new String[] { textField_3.getText() }, 2);
                        } else {
                            // prompt user and ensures that one does not open
                            // multiple tabs to the same user
                            JOptionPane.showMessageDialog(null, "You are already connected to " + "Chat with "
                                    + textField_3.getText(), "alert", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        panel.add(btnNewButton_1);

        // set the tabbed pane to top
        tabbedPane = new JTabbedPane(SwingConstants.TOP);

        // add tabbed pane to center of content pane
        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

}
