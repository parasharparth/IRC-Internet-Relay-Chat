package version2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/*******************************************************************************************
 * Client class consisting of the data members 
 * Data members are then used for determining the properties of the client characteristics
 *******************************************************************************************/
public class Client extends JFrame implements ActionListener {
	
	
/*******************************************************************************************************************
  * Serial ID for determining the serialize number for JVM to create separate pools of threads for client and server 
*******************************************************************************************************************/
	private static final long serialVersionUID = 1L;

/*******************************************************************************************************************
 * Client Data Members
 * socket:- For initializing the socket connection
 * shutdown:- Variable for graceful shutdown of the menu
 * ObjectOutputStream:- For sending data to the server
 * ObjectInputStream:- For receiving the input from the server (IP, PORT and handshake flags)
 * ExecutorService pool:- For determining the maximum number of threads that can be executed by the thread pool
 * PacketListener packetListener:- For continuous receiving of the inputs and outputs of the server
 *****************************************************************************************************************/
  private Socket socket;
  private boolean shutdown;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private ExecutorService pool;
  private PacketListener packetListener;

  
  /************************************************************************************************************** 
   * GUI Data Members 
   *  loginMenu:- This will handle the login menu for the client
   * In the login menu, we have,
   * textField:- for host name and port number (default value at host (for local host) and 8080 respectively)
   * textArea:- for printing the greeting message, user id display, and the room number display
   *************************************************************************************************************/
  private LoginMenu loginMenu;
  private JTextArea chatDisplay;
  private JTextField textInput;
  private JTextArea userDisplay;
  private JTextArea roomDisplay;

  
  /***************************************************************************************************
   * Constructor Initializes the client object by running the GUI setup functions and setting the
   * login menu to visible to the user.
   ***************************************************************************************************/
  Client() {
    super("IRC Client");
    System.out.println("Starting up client application...");
    clientGUISetup();
    loginMenu = new LoginMenu();
    loginMenu.setVisible(true);
    System.out.println("Success! Client application started.");
  }

  
  /**************************************************************** 
   * Graceful Exit of the Client windows with exception handling
   * Exits the application with status code 0.
   ****************************************************************/
  private void closeClientApplication() {
    System.out.println("Closing client application...");
    System.exit(0);
  }

  
  /******************************************************************************************************
   * Sends out a socket connection request to ip:port. If successful, attempts to instantiate the
   * object streams, the single thread pool (for holding the packet listener), and the packet listener. 
   * Finally it starts up the packet listener by executing it in the pool and returning true. 
   * If any exceptions occur, it returns false.
   * @param ip a string representing the desired IP address that is being connected to
   * @param port an integer representing the desired port number that is being connected to
   * @return a boolean representing whether successful connection to server was made
   *******************************************************************************************************/
  private boolean connectToServer(String ip, int port) {
    System.out.println("Connecting to server...");
    shutdown = false;
    try {
      socket = new Socket(ip, port);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
      pool = Executors.newFixedThreadPool(1);
      packetListener = new PacketListener();
      pool.execute(packetListener);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    System.out.println("Success! Connected to server.");
    return true;
  }

  
  /********************************************************************************************************
   * Sets shutdown to true, thus exiting the infinite incoming connection loop and closing the server.
   *  Also sends a final packet to the server to let it know the user is logging out of the server.
   *******************************************************************************************************/
  private void disconnectFromServer() {
    System.out.println("Disconnecting from server...");
    shutdown = true;
    Packet packet = new Packet();
    packet.leaveServer();
    sendPacket(packet);
  }

  
  /*******************************************************************************************************
   * Closes all connections and sets the relevant members to their null values. 
   * Finally switches the visible windows from the chat window to the login window.
   ******************************************************************************************************/
  private void serverDisconnectCleanup() {
    // disconnect sequence
    System.out.println("Closing connections...");
    try {
      in.close();
      in = null;
      out.close();
      out = null;
      socket.close();
      socket = null;
      pool.shutdown();
      pool = null;
      packetListener = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Success! Connections closed.");
    setVisible(false);
    loginMenu.displayFeedback("Disconnected from server.");
    loginMenu.setVisible(true);
  }

  
  /********************************************************
   * Sends a given packet to the server.
   * @param packet packet to be sent to the server
   ********************************************************/
  private void sendPacket(Packet packet) {
    try {
      out.writeObject(packet);
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(packet.command + " packet sent to server.");
  }

  
  /*********************************************************************************************************************************
   * Takes a given packet and inspects its command value to route it to the correct function with the correct data passed to it. 
   * Any unrecognized packet command types are ignored.
   * @param packet packet to be inspected and rerouted.
   *********************************************************************************************************************************/
  private void packetHandler(Packet packet) {
    String command = packet.command;
    switch (command) {
      case "joinServer": // username logged by server, ready to start chatting
        startChatGUI(packet.message);
        break;
      case "userUpdate":
        userDisplay.setText(packet.message);
        break;
      case "roomUpdate":
        roomDisplay.setText(packet.message);
        break;
      case "displayToUser":
        displayToUser(packet.message);
        break;
      case "shutdown":
        shutdown = true;
        break;
      default:
        // TODO - error handling
    }
  }


  /*********************************************************** 
   * Initializes the GUI for the client 
   * GUI Methods
   * setDefaultCloseOperation():- For stopping the client
   **********************************************************/
  private void clientGUISetup() {
    setSize(900, 500);
    setResizable(false);
    
    // call stopClient function on close
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            disconnectFromServer();
          }
        });

    
    // panel holding all individual displays in grid bag layout
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    add(panel);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(3, 3, 3, 3);
    gbc.fill = GridBagConstraints.VERTICAL;

    // initialize active user list display
    userDisplay = new JTextArea(25, 15);
    userDisplay.setLineWrap(false);
    userDisplay.setEditable(false);
    JScrollPane userDisplayScroll = new JScrollPane(userDisplay);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 3;
    panel.add(userDisplayScroll, gbc);

    // initializes active room list display
    roomDisplay = new JTextArea(25, 15);
    roomDisplay.setLineWrap(false);
    roomDisplay.setEditable(false);
    JScrollPane roomDisplayScroll = new JScrollPane(roomDisplay);
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 3;
    panel.add(roomDisplayScroll, gbc);

    // initialize chat dialogue display
    chatDisplay = new JTextArea(25, 40);
    chatDisplay.setText("System: Welcome to the Chat Server!");
    chatDisplay.setLineWrap(true);
    chatDisplay.setEditable(false);
    JScrollPane chatDisplayScroll = new JScrollPane(chatDisplay);
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.gridheight = 1;
    panel.add(chatDisplayScroll, gbc);

    // initialize text input field
    textInput = new JTextField(33);
    textInput.addActionListener(this);
    gbc.gridx = 2;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    panel.add(textInput, gbc);

    // initialize send button
    JButton sendButton = new JButton("Send");
    sendButton.addActionListener(this);
    gbc.gridx = 3;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    panel.add(sendButton, gbc);
  }

  
  /******************************************************************************************
   * Reveals the chat GUI window with a given message, clearing out any previous messages.
   * @param message string set as the only text in the chat window display
   ******************************************************************************************/
  private void startChatGUI(String message) {
    loginMenu.setVisible(false);
    setVisible(true);
    System.out.println("Ready to start chatting!");
    chatDisplay.setText(message);
  }

  
  /*************************************************************************************************************
   * Takes a string and posts it to the chat window display with the correct formatting. 
   * Also sets the character position so that the window scrolls as you get new messages and shows the most recent.
   * @param message message to be posted to the chat window display
   *************************************************************************************************************/
  private void displayToUser(String message) {
    chatDisplay.append("\n" + message);
    chatDisplay.setCaretPosition(chatDisplay.getDocument().getLength());
  }

  
  /******************************************************************************************************************************
   * @summary:- determining what @<command> maps to
   * Takes a string and parses it to determine if it is a command. 
   * If it is determined to be a command (starting with '\@'), then it is further parsed to determine ->
   * which kind and does the necessary error checking for malformed arguments. 
   * If any arguments are malformed, it outputs an error message to the user describing the malformed argument. 
   * If the input is not a command, it assumes it is a message to be sent to all users. 
   * Finally, once the input is parsed, the packet is sent to the server with the correct command argument.
   * @param userInput
   ****************************************************************************************************************************/
  private void parseInput(String userInput) {
    Packet packet = new Packet();
    if (userInput.startsWith("@")) {
      String[] input = userInput.split(" ", 2);
      int targetid;
      if (input.length < 2)
        displayToUser("System: Insufficient arguments provided for command '" + userInput + "'.");
      switch (input[0]) {
        case "@user":
          input = input[1].split(" ", 2);
          try {
            targetid = Integer.parseInt(input[0]);
          } catch (Exception e) {
            displayToUser(
                "System: '" + input[0] + "' in command '" + userInput + "' is not a valid number.");
            return;
          }
          if (input[1].equals("")) {
            displayToUser("System: Cannot send an empty message to a user.");
            return;
          }
          packet.sendMessageUser(targetid, userInput);
          sendPacket(packet);
          break;
        case "@room":
          input = input[1].split(" ", 2);
          try {
            targetid = Integer.parseInt(input[0]);
          } catch (Exception e) {
            displayToUser(
                "System: '" + input[0] + "' in command '" + userInput + "' is not a valid number.");
            return;
          }
          if (input[1].equals("")) {
            displayToUser("System: Cannot send an empty message to a room.");
            return;
          }
          packet.sendMessageRoom(targetid, userInput);
          sendPacket(packet);
          break;
        case "@create":
          if (input[1].equals("")) {
            displayToUser("System: Cannot create a room with no name.");
            return;
          }
          packet.createRoom(input[1]);
          sendPacket(packet);
          break;
        case "@join":
          if (input.length > 2) {
            displayToUser("System: Too many arguments provided in command '" + userInput + "'.");
            return;
          }
          try {
            targetid = Integer.parseInt(input[1]);
          } catch (Exception e) {
            displayToUser(
                "System: '" + input[1] + "' in command '" + userInput + "' is not a valid number.");
            return;
          }
          packet.joinRoom(targetid);
          sendPacket(packet);
          break;
        case "@leave":
          if (input.length > 2) {
            displayToUser("System: Too many arguments provided in command '" + userInput + "'.");
            return;
          }
          try {
            targetid = Integer.parseInt(input[1]);
          } catch (Exception e) {
            displayToUser(
                "System: '" + input[1] + "' in command '" + userInput + "' is not a valid number.");
            return;
          }
          packet.leaveRoom(targetid);
          sendPacket(packet);
          break;
        default:
          String message =
              "System: Unrecognized request '"
                  + input[0]
                  + "' in command '"
                  + userInput
                  + "'."
                  + "\n   Recognized Requests: "
                  + "\n      @user <user id #> <message>"
                  + "\n      @room <room id #> <message>"
                  + "\n      @create <room name>"
                  + "\n      @join <room id #>"
                  + "\n      @leave <room id #>";
          displayToUser(message);
      }
    } else {
      packet.sendMessageAll(userInput);
      sendPacket(packet);
    }
  }

  
  /***********************************************************************************************
   * Takes an action event interaction with the chat window GUI, and grabs the text from the user
   * input text box and hands it to the parseInput() function for interpreting and manipulation.
   * @param event ActionEvent object created by the GUI interaction
   **********************************************************************************************/
  public void actionPerformed(ActionEvent event) {
    String userInput = textInput.getText();
    if (userInput.equals("")) return;
    textInput.setText("");
    parseInput(userInput);
  }

  
  /*********************************************************************************************************************************************
   * @summary:- 
   * The PacketListener is a runnable thread which will a-synchronously AS WELL AS synchronously listen for incoming packets from the server. 
   * The run function is called when the PacketListener is handed to the thread pool and executed.
   * It loops while the shutdown member is false, attempting to read in packets from the server. 
   * On any failure to read from the server, it calls the disconnectFromServer()  function to close the connection and leave the server. 
   * When it gets a packet from the server, it hands it to the packetHandler() function to be interpreted and manipulated. 
   * Once it exits the loop, it calls the serverDisconnectCleanup() function to close all the connections and return to the login GUI.
   * @implNote:- Runnable Interface
   * @implSpec:- overridden run() method responsible for the client to the spawned 
   * @serverDisconnectCleanup():- method is responsible for freeing up the resources(from the thread-pool)
   ******************************************************************************************************************************************/
  private class PacketListener implements Runnable {
	  
    @Override
    public void run() {
      // packet listening loop
      System.out.println("Listening for packets...");
      while (!shutdown) {
        try {
          Packet packet = (Packet) in.readObject();
          System.out.println(packet.command + " packet received from server.");
          packetHandler(packet);
        } catch (Exception e) {
          if (e instanceof EOFException) disconnectFromServer();
          else e.printStackTrace();
        }
      }
      serverDisconnectCleanup();
    }
  }

  
  /*********************************************************************************************************
   * The login menu is the GUI object that the user utilizes to instigate connections to the server.
   *********************************************************************************************************/
  private class LoginMenu extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	/************************************************************************************************
	 * Data Members for the login menu after a client starts/exits 
	 * feedback :- For getting the proper feedback from the client
	 * portField:- For printing the port number on the start/exit screen
	 * usernameField:-For printing the user-name on the start/exit screen
	 * startButton:- For start button for restarting the server
	 * clearButton:- For clearing the menu
	 **********************************************************************************************/
    JTextArea feedback;

    JTextField ipField;
    JTextField portField;
    JTextField usernameField;
    JButton connectButton;
    JButton clearButton;

    
    /***********************************************************************************************
     *  Constructor - calls the GUI initialization for the login window 
     ***********************************************************************************************/
    LoginMenu() {
      super("IRC Client");
      loginGUISetup();
    }

    
    // Methods
    /***********************************************************************************************
     * initializes the GUI for the login menu 
     ***********************************************************************************************/
    private void loginGUISetup() {
      setSize(550, 250);
      setResizable(false);
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(
          new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
              super.windowClosing(event);
              closeClientApplication();
            }
          });

      JPanel panel = new JPanel();
      add(panel);

      
      // initialize feedback display
      feedback = new JTextArea(5, 40);
      feedback.setText("Welcome to the IRC Client!");
      feedback.setLineWrap(true);
      feedback.setEditable(false);
      panel.add(new JScrollPane(feedback));

      
      // initialize ip address field
      panel.add(new JLabel("IP Address"));
      ipField = new JTextField();
      ipField.setColumns(33);
      ipField.setText("localhost");
      panel.add(ipField);

      
      // initialize port number field
      panel.add(new JLabel("Port Number"));
      portField = new JTextField();
      portField.setColumns(33);
      portField.setText("8080");
      panel.add(portField);

      
      // initialize username field
      panel.add(new JLabel("Username"));
      usernameField = new JTextField();
      usernameField.setColumns(33);
      usernameField.setText("anon");
      panel.add(usernameField);

      // initialize connect button
      connectButton = new JButton("Connect to Server");
      connectButton.addActionListener(this);
      panel.add(connectButton);

      // initialize clear button
      clearButton = new JButton("Clear");
      clearButton.addActionListener(this);
      panel.add(clearButton);
    }

    /***********************************************************************************************
     * appends feedback to the feedback display in the login menu
     * @param message - String of feedback to be appended to display
     **********************************************************************************************/
    private void displayFeedback(String message) {
      feedback.append("\n" + message);
      feedback.setCaretPosition(feedback.getDocument().getLength());
    }

    /***********************************************************************************************
     * on an action event which occurs in the login menu, this function is called
     * @param event - ActionEvent object representing an event that has occurred in the login menu
     **********************************************************************************************/
    public void actionPerformed(ActionEvent event) {
      // connect button pressed
      if (event.getSource() == connectButton) {
        String ip = ipField.getText();
        String portString = portField.getText();
        String username = usernameField.getText();
        if (ip.equals("") || portString.equals("") || username.equals("")) {
          displayFeedback("Please fill out the necessary fields to connect.");
          return;
        }
        int port;
        try {
          port = Integer.parseInt(portString);
        } catch (Exception e) {
          displayFeedback(portString + " is not a valid port number.");
          return;
        }
        displayFeedback("Attempting to connect to server " + ip + ":" + portString + "...");
        if (!connectToServer(ip, port)) {
          displayFeedback("Connection failed.");
          return;
        }
        displayFeedback("Success! Connected to server " + ip + ":" + portString);
        Packet packet = new Packet();
        packet.joinServer(username);
        sendPacket(packet);
      }
      // clear button pressed
      if (event.getSource() == clearButton) {
        ipField.setText("");
        portField.setText("");
        usernameField.setText("");
      }
    }
  }
  
  /*******************************************************************************************
   * This is the starting point of the Client class
   * The client constructor is called which is then responsible for calling the consecutive
   * @param args
   *******************************************************************************************/

  public static void main(String[] args) {
    @SuppressWarnings("unused")
	Client client = new Client();
  }
}