package version2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
 

/******************************************************************************************************************************************
 * {@summary}
 * The server class enables communication, acting as the central point of contact for all messages sent through the application. 
 * Once the server is started on a valid port, clients can connect to the server. 
 * The server will initialize a thread pool, running an infinite loop to listen for incoming connection requests from the client. 
 * When the client sends a message, the server will receive its first, 
 * it will then transmit the message to all connected users, 
 * or to a particular user, or to a virtual room specified by the client.
 *****************************************************************************************************************************************/
public class Server extends JFrame implements ActionListener {
  
	private static final long serialVersionUID = 1L;
	
/****************************************************************************************************************************************
 * These are the server data members which are used to uniquely identify the connection
 * @serialField pool
 * @docRoot main
 * @implSpec threadLimit = 20 
 * @summary :- Here the maximum number of clients which can be added to server is capped at 20
 * This is by the virtue of the number of threads being created by the server.
 * The other variables include:- 
 * connectionListener :- This will keep the server in an infinite loop listening for new clients
 * serverSocket pool :- This is used for managing the server thread pool properly by using synchronize keyword
 * serverHosted:- tells the number (id) of the server on which the client is being hosted
 * shutdown:- For shutdown purposes
 * threadCount:- For keeping a count on the number of threads
 * roomCount:- For keeping a count on the number of rooms
 * threadMap:- Keeps the threads and their states stored in a Map. This is then used for mapping of user id #s to ServerThreads
 * roomMap:- Stores the rooms and their states stored in a Map. This is then used for mapping of room id #s to ServerRooms
 *******************************************************************************************************************************************/
  private int threadLimit = 20;
  private ConnectionListener connectionListener;
  private ServerSocket serverSocket;
  private ExecutorService pool;
  private boolean serverHosted;
  private boolean shutdown;
  private int threadCount;
  private int roomCount;
  private Map<Integer, ServerThread> threadMap;
  private Map<Integer, ServerRoom> roomMap;
  

/*************************************************************************************************************************************
 * These are the member variables which are then used to manage the characteristics for the GUI fields
 * The fields that these will be managing are:- 
 * loginMenu:- This will handle the login menu for the server
 * In the login menu, we have,
 * textField:- for host name and port number (default value at host (for local host) and 8080 respectively)
 * textArea:- for printing the greeting message, user id display, and the room number display
 *************************************************************************************************************************************/
  private LoginMenu loginMenu;
  private String hostname;
  private JTextArea chatDisplay;
  private JTextField textInput;
  private JTextArea userDisplay;
  private JTextArea roomDisplay;

  
  /*********************************************************************************************************************************************
   * Constructor initializes the Graphical User Interface (GUI). 
   * Once started, closing this window will stop the server with the help of an actionListener associated with the mouse click on close event
   *********************************************************************************************************************************************/
  private Server() {
    super("IRC Server");
    System.out.println("Starting up server application...");
    serverGUISetup();
    loginMenu = new LoginMenu();
    loginMenu.setVisible(true);
    System.out.println("Success! Server application started.");
  }

  
  /******************************************************************************************************
   * If a user closes the application window, this method will be called to shut down the server.
   * @stopServer() method is used to close the infinite loop listening for incoming connection requests
   *****************************************************************************************************/
  private void closeServerApplication() {
    System.out.println("Closing server application...");
    if (serverHosted) {
      stopServer(); 
      while (serverHosted) {
        try {
          Thread.sleep(1000);
          System.out.print(".");
        } catch (Exception e) {
          System.out.println("Some exception has occured during shutting the server down. EXITING THE APPLICATION");
          System.exit(1);
        }
      }
      System.out.println();
    }
    System.out.println("See ya next time!");
    System.exit(0);
  }

  
  /**************************************************************************************************************************************************
   * Initializes the server to a clean state with the following attributes:
   * 1) A functioning socket that can listen for incoming client connections 
   * 2) A fresh thread pool-> Additionally, the GUI context will be switched from Login to Running.
   * 3) ThreadLimit is used here to limit the number of threads using the Executors.newFixedThreadPool(Integer)
   * 4) resetChatGUI() - resetting the fields in the server GUI window to zero/ default values if assigned.
   * 5) setVisible() - for determining when to show the chat window
   * 6) loginMenu.setVisible(false) :- for determining when to hide the server opening window after a server connection has been established
   **************************************************************************************************************************************************/
  private boolean startServer(int port, String username) {
    System.out.println("Attempting to host server...");
    hostname = username;
    shutdown = false;
    threadCount = 0;
    roomCount = 0;
    threadMap = new HashMap<>();
    roomMap = new HashMap<>();
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(1000);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    pool = Executors.newFixedThreadPool(threadLimit); 
    System.out.println("Success! Server now hosted on port " + port + ".");
    serverHosted = true;
    resetChatGUI(); 
    setVisible(true);
    loginMenu.setVisible(false); 
    return true;
  }

  
  /****************************************************************************************************************************
   * Once the server is started, a connection listener is executed to listen to incoming connection requests from the client.
   ****************************************************************************************************************************/
  private void runConnectionListener() {
    connectionListener = new ConnectionListener();
    pool.execute(connectionListener);
  }

  
  /*********************************************************************************************************
   * Sets shutdown to true, thus exiting the infinite incoming connection loop and stopping the server.
   *********************************************************************************************************/
  private void stopServer() {
    System.out.println("Stopping server...");
    Packet packet = new Packet();
    packet.shutdown();
    sendPacketAll(packet);
    shutdown = true;
  }

  
  /********************************************************************************************************************
   * Once the server is shut down, all fields are GUI properties are reset to their original state, or null values.
   *******************************************************************************************************************/
  private void serverShutdownCleanup() {
    try {
      threadMap = null;
      roomMap = null;
      pool.shutdown();
      pool = null;
      connectionListener = null;
      serverSocket.close();
      serverSocket = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
    serverHosted = false;
    loginMenu.resetLoginGUI();
    setVisible(false);
    loginMenu.setVisible(true);
  }

  
  /*********************************************************************************************************************
   * Takes a packet from a specified client and determines what action to take given the packet's command value.
   * @param packet packet containing data from the client
   * @param senderid id number corresponding to the client that sent the packet
   **********************************************************************************************************************/
  private void packetHandler(Packet packet, int senderid) {
    String command = packet.command;
    switch (command) {
      case "joinServer":
        joinServer(senderid, packet.message);
        break;
      case "leaveServer":
        disconnectClient(senderid);
      case "sendMessageAll":
        sendMessageAll(senderid, packet.message);
        break;
      case "sendMessageUser":
        sendMessageUser(senderid, packet.targetid, packet.message);
        break;
      case "sendMessageRoom":
        sendMessageRoom(senderid, packet.targetid, packet.message);
        break;
      case "createRoom":
        createRoom(senderid, packet.message);
        break;
      case "joinRoom":
        joinRoom(senderid, packet.targetid);
        break;
      case "leaveRoom":
        leaveRoom(senderid, packet.targetid);
        break;
      default:
        // TODO - error handling
    }
  }

  /****************************************************************************************************************************
   * When a client connects to the server, or disconnects from the server, the user list is updated to reflect this change.
   ****************************************************************************************************************************/
  private void userUpdate() {
    StringBuilder sb = new StringBuilder();
    sb.append(threadMap.size()).append(" USERS\n");
    for (Map.Entry<Integer, ServerThread> entry : threadMap.entrySet())
      sb.append("\n# ").append(entry.getKey()).append(" ").append(entry.getValue().username);
    userDisplay.setText(sb.toString());
    Packet packet = new Packet();
    packet.userUpdate(sb.toString());
    sendPacketAll(packet);
  }

  
  /**********************************************************************************************************
   * When a room is created, or is removed (this occurs when all users have disconnected from a
   * particular room), the room list is updated to reflect this change.
   **********************************************************************************************************/
  private void roomUpdate() {
    StringBuilder sb = new StringBuilder();
    sb.append(" ROOMS\n");
    for (Map.Entry<Integer, ServerRoom> entry : roomMap.entrySet()) {
      if (entry.getValue().members.isEmpty()) {
        roomMap.remove(entry.getKey());
        continue;
      }
      sb.append("\n# ").append(entry.getKey()).append(" ").append(entry.getValue().roomName);
      for (Integer i : entry.getValue().members)
        sb.append("\n   # ").append(i).append(" ").append(threadMap.get(i).username);
    }
    roomDisplay.setText(sb.toString());
    Packet packet = new Packet();
    packet.roomUpdate(roomMap.size() + sb.toString());
    sendPacketAll(packet);
  }

  
  /***************************************************************************************************
   * Sends the packet containing data from the client to every connected user. 
   * The threadMap contains every user id, which corresponds to the server thread count.
   * @param packet packet containing data from the client
   ***************************************************************************************************/
  private void sendPacketAll(Packet packet) {
    for (Map.Entry<Integer, ServerThread> entry : threadMap.entrySet())
      entry.getValue().sendPacket(packet);
  }

  
  /******************************************************************************************************************
   * Once a new client has connected to the server, the user id and user-name is stored, status
   * messages are displayed to the user, and the user list and room list is updated to account for the new client.
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param username the user-name entered by the client upon connecting to the server
   *****************************************************************************************************************/
  private void joinServer(int senderid, String username) {
    displayToUser("System: User # " + senderid + " has joined the chat as " + username + ".");
    ServerThread serverThread = threadMap.get(senderid);
    serverThread.username = username;
    Packet packet = new Packet();
    packet.joinServer(
        "System: Welcome to the server, " + username + "! Your user id # is " + senderid + ".");
    serverThread.sendPacket(packet);
    userUpdate();
    roomUpdate();
  }

  
  /**************************************************************************************************************
   * Once a client disconnects from the server, the user id and user-name is removed, status messages ./n
   * are displayed to the user, and the user list and room list is updated to account for the change.
   * @param senderid the unique identification number corresponding to the client that performed the action
   *************************************************************************************************************/
  private void disconnectClient(int senderid) {
    ServerThread serverThread = threadMap.get(senderid);
    threadMap.remove(senderid);
    displayToUser(
        "System: User # " + senderid + " (" + serverThread.username + ") has left the chat.");
    serverThread.shutdownThread = true;
    for (Map.Entry<Integer, ServerRoom> entry : roomMap.entrySet())
      entry.getValue().removeUser(senderid);
    userUpdate();
    roomUpdate();
  }

  
  /**********************************************************************************************************************
   * Broadcasts a message to all connected users. This is the default behavior when text is entered without a command.
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param message the text the client entered to send to all connected users
   *********************************************************************************************************************/
  private void sendMessageAll(int senderid, String message) {
    Packet packet = new Packet();
    String output = threadMap.get(senderid).username + " (# " + senderid + "): " + message;
    displayToUser(output);
    packet.displayToUser(output);
    sendPacketAll(packet);
  }

  
  /*******************************************************************************************************************************************
   * Sends a message from the client (sender-id) to another user (target-id).
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param targetid the unique identification number corresponding to the target client (recipient) of the client's intended action)
   * @param message the text the client entered to send to a specific user
   ******************************************************************************************************************************************/
  private void sendMessageUser(int senderid, int targetid, String message) {
    ServerThread serverThread = threadMap.get(targetid);
    if (serverThread == null) {
      sendError(senderid, "System: User id # " + targetid + " not found.");
      return;
    }
    String output = threadMap.get(senderid).username + " (# " + senderid + "): " + message;
    displayToUser(output);
    Packet packet = new Packet();
    packet.displayToUser(output);
    serverThread.sendPacket(packet);
    threadMap.get(senderid).sendPacket(packet);
  }

  
  /**************************************************************************************************************
   * Sends a message from the client (sender-id) to all connected users in a particular room (target-id).
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param targetid the unique identification number corresponding to a particular room
   * @param message the text the client entered to send to users connected to a particular room
   *************************************************************************************************************/
  private void sendMessageRoom(int senderid, int targetid, String message) {
    ServerRoom serverRoom = roomMap.get(targetid);
    if (serverRoom == null) {
      sendError(senderid, "System: Room id # " + targetid + " not found.");
      return;
    }
    if (!serverRoom.members.contains(senderid)) {
      StringBuilder sb = new StringBuilder();
      sb.append("System: You are not a member of room '").append(serverRoom.roomName);
      sb.append("' (id # ").append(targetid).append("). ");
      sb.append(" You cannot send a message to a room you aren't in.");
      sendError(senderid, sb.toString());
      return;
    }
    String output = threadMap.get(senderid).username + " (# " + senderid + "): : " + message;
    displayToUser(output);
    Packet packet = new Packet();
    packet.displayToUser(output);
    for (Integer i : serverRoom.members) threadMap.get(i).sendPacket(packet);
  }

  
  /**************************************************************************************************************
   * Creates a new virtual room, with the room name specified by the client.
   * Once the room is created, the client automatically joins the room. 
   * The room list is updated accordingly.
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param roomName the room name the client entered
   *************************************************************************************************************/
  private void createRoom(int senderid, String roomName) {
    ServerRoom serverRoom = new ServerRoom(senderid, roomName);
    ++roomCount;
    roomMap.put(roomCount, serverRoom);
    roomUpdate();
    Packet packet = new Packet();
    packet.displayToUser(
        "System: Room '"
            + roomName
            + "' has been created under id # "
            + roomCount
            + " with you in it.");
    threadMap.get(senderid).sendPacket(packet);
  }

  /**************************************************************************************************************
   * A client (sender-id) may join a specific room (target-id), which will allow them to send and
   * receive messages to/from users connected to that room. 
   * The room list is updated to reflect the new user who joined.
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param targetid the unique identification number corresponding to a particular room
   *************************************************************************************************************/
  private void joinRoom(int senderid, int targetid) {
    ServerRoom serverRoom = roomMap.get(targetid);
    if (serverRoom == null) {
      sendError(senderid, "System: Room id # " + targetid + " not found.");
      return;
    }
    if (serverRoom.members.contains(senderid)) {
      sendError(
          senderid,
          "System: You are already a member of room '"
              + serverRoom.roomName
              + "' (id # "
              + targetid
              + ").");
      return;
    }
    serverRoom.members.add(senderid);
    roomUpdate();
    Packet packet = new Packet();
    packet.displayToUser(
        "System: You have joined room '" + serverRoom.roomName + "' with id # " + roomCount + ".");
    threadMap.get(senderid).sendPacket(packet);
  }

  
  /******************************************************************************************************************************************************************
   * A client (sender-id) may leave a specific room (target-id), which will disable them from sending and receiving messages to/from users connected to that room. 
   * The room list is updated to remove the user who left. Once all users have disconnected from a room, the room is destroyed.
   * @param senderid the unique identification number corresponding to the client that performed the action
   * @param targetid the unique identification number corresponding to a particular room
   ******************************************************************************************************************************************************************/
  private void leaveRoom(int senderid, int targetid) {
    ServerRoom serverRoom = roomMap.get(targetid);
    if (serverRoom == null) {
      sendError(senderid, "System: Room id # " + targetid + " not found.");
      return;
    }
    if (!serverRoom.members.contains(senderid)) {
      sendError(
          senderid,
          "System: You are not a member of room '"
              + serverRoom.roomName
              + "' (id # "
              + targetid
              + ").");
      return;
    }
    String roomName = serverRoom.roomName;
    serverRoom.removeUser(senderid);
    roomUpdate();
    Packet packet = new Packet();
    packet.displayToUser(
        "System: You have left room '" + roomName + "' with id # " + targetid + ".");
    threadMap.get(senderid).sendPacket(packet);
  }

  
  /**************************************************************************************************************
   * Graceful error handling, particularly useful in the event that a target user is not found.
   * @param targetid the unique identification number corresponding to the target client
   * @param message the text to display to the client
   *************************************************************************************************************/
  private void sendError(int targetid, String message) {
    ServerThread serverThread = threadMap.get(targetid);
    if (serverThread == null) {
      System.out.println(
          "Attempted to send error packet to id # "
              + targetid
              + ", but was not found in threadMap.");
      return;
    }
    Packet packet = new Packet();
    packet.displayToUser(message);
    serverThread.sendPacket(packet);
  }

  
  /****************************************** 
   * Initializes the GUI for the server. 
   ****************************************/
  private void serverGUISetup() {
    setSize(900, 500);
    setResizable(false);
    
    // call stopServer function on close
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent event) {
            super.windowClosing(event);
            stopServer();
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

  
  /*****************************************************
   * Displays message to user in GUI.
   * @param message the text to display
   ******************************************************/
  private void displayToUser(String message) {
    chatDisplay.append('\n' + message);
    chatDisplay.setCaretPosition(chatDisplay.getDocument().getLength());
  }

  
  /****************************************************
   * Resets all values that appear in the GUI. 
   ****************************************************/
  private void resetChatGUI() {
    chatDisplay.setText("System: Welcome to the Chat Server!");
    userDisplay.setText("0 USERS");
    roomDisplay.setText("0 ROOMS");
  }

  
  /************************************************
   * Parses input received from the client.
   * @param userInput textual input from the user
   ***********************************************/
  private void parseInput(String userInput) {
    Packet packet = new Packet();
    if (userInput.startsWith("@")) {
      // TODO - implement special cases
    } else {
      String message = hostname + ": " + userInput;
      packet.displayToUser(message);
      sendPacketAll(packet);
      displayToUser(message);
    }
  }

  /***************************************************************************
   *  Returns if user input is blank; else, parses the user's textual input.
   ***************************************************************************/
  public void actionPerformed(ActionEvent event) {
    String userInput = textInput.getText();
    if (userInput.equals("")) return;
    textInput.setText("");
    parseInput(userInput);
  }

  
  /************************************************************************************************************
   * Initializes thread pool and runs infinite loop to listen for incoming connection requests.
   * Exits the loop upon call of stopServer, which sets shutdown to true, thus exiting the loop and cleaning up.
   *************************************************************************************************************/
  private class ConnectionListener implements Runnable {
	
    /************************************************************************************************************** 
     * Runs an infinite loop to listen for incoming connection requests from the client. 
     *************************************************************************************************************/
    @Override
    public void run() {
      ExecutorService pool = Executors.newFixedThreadPool(threadLimit);
      
      // loop for accepting client connection requests
      while (!shutdown) {
        try {
          Socket clientSocket = serverSocket.accept();
          ++threadCount;
          System.out.println("New user connected - id # " + threadCount);
          displayToUser("System: User # " + threadCount + " connected to server.");
          ServerThread serverThread = new ServerThread(clientSocket, threadCount);
          pool.execute(serverThread);
          threadMap.put(threadCount, serverThread);
        } catch (Exception e) {
          if (e instanceof SocketTimeoutException) continue;
          e.printStackTrace();
          if (!(e instanceof SocketException)) {
            System.exit(1);
          }
        }
      }
      
      // shutdown sequence once loop breaks
      serverShutdownCleanup();
    }
  }

  
  /**************************************************************************************************************************
   * Invoked by the ConnectionListener class, the Server Thread class runs an infinite loop to listen for incoming packets. 
   * Server threads store unique identification numbers corresponding to connected users and rooms.
   **************************************************************************************************************************/
  private class ServerThread implements Runnable {
    Socket clientSocket;
    int id;
    String username;
    ObjectOutputStream out;
    ObjectInputStream in;
    boolean shutdownThread;

    /* Constructor */
    ServerThread(Socket clientSocket, int id) {
      System.out.println("Initializing user id # " + id + "...");
      shutdownThread = false;
      this.clientSocket = clientSocket;
      this.id = id;
      try {
        out = new ObjectOutputStream(this.clientSocket.getOutputStream());
        in = new ObjectInputStream(this.clientSocket.getInputStream());
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
      System.out.println("Done.");
    }

    
    /******************************************************** 
     * Runs an infinite loop to listen for incoming packets. 
     ********************************************************/
    @Override
    public void run() {
    	
      // listening loop
      while (!shutdownThread) {
        try {
          Packet packet = (Packet) in.readObject();
          System.out.println(packet.command + " packet received from user id # " + id + ".");
          packetHandler(packet, id);
        } catch (Exception e) {
          if (e instanceof EOFException) shutdownThread = true;
          else e.printStackTrace();
        }
      }
      
      // thread shutdown sequence
      System.out.println("Closing connection to user id # " + id + "...");
      try {
        out.close();
        in.close();
        clientSocket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println("Done.");
    }

    
    /*********************************************************
     * Writes data contained in packet to an output stream.
     * @param packet packet containing data from the client
     *********************************************************/
    private void sendPacket(Packet packet) {
      try {
        out.writeObject(packet);
        out.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println(packet.command + " packet sent to user id # " + id + ".");
    }
  }

  
  /*********************************************************************************************
   *  Object holding user identification numbers for users that are members of a given room. 
   **********************************************************************************************/
  private static class ServerRoom {
    String roomName;
    Vector<Integer> members;

    /* Constructor */
    ServerRoom(int initialMember, String roomName) {
      this.roomName = roomName;
      members = new Vector<>();
      members.add(initialMember);
    }

    
    /**************************************************************************
     * Removes the client corresponding to the given id number from a room.
     * @param targetid
     **************************************************************************/
    void removeUser(int targetid) {
      members.removeIf(i -> i == targetid);
    }
  }

  
  /*************************************
   *  Graphical User Interface (GUI) 
   *************************************/
  private class LoginMenu extends JFrame implements ActionListener {
    
	private static final long serialVersionUID = 1L;
	
	/*******************************************************************
	 * Data Members for the login menu after a client starts/exits 
	 * feedback :- For getting the proper feedback from the client
	 * portField:- For printing the port number on the start/exit screen
	 * usernameField:-For printing the user-name on the start/exit screen
	 * startButton:- For start button for restarting the server
	 * clearButton:- For clearing the menu
	 *****************************************************************/
    JTextArea feedback;
    JTextField portField;
    JTextField usernameField;
    JButton startButton;
    JButton clearButton;

    /************************************************************************************
     * Constructor which is called for starting the login menu from the main GUI window
     ************************************************************************************/
    LoginMenu() {
      super("IRC Server");
      loginGUISetup();
    }

    /************************************************* 
     * Initializes the GUI for the login menu
     ************************************************/
    private void loginGUISetup() {
      setSize(550, 250);
      setResizable(false);
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(
          new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
              super.windowClosing(event);
              closeServerApplication();
            }
          });

      JPanel panel = new JPanel();
      add(panel);

      // initialize feedback display
      feedback = new JTextArea(5, 40);
      feedback.setLineWrap(true);
      feedback.setEditable(false);
      panel.add(new JScrollPane(feedback));

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
      usernameField.setText("HOST");
      panel.add(usernameField);

      // initialize connect button
      startButton = new JButton("Start Server");
      startButton.addActionListener(this);
      panel.add(startButton);

      // initialize clear button
      clearButton = new JButton("Clear");
      clearButton.addActionListener(this);
      panel.add(clearButton);
      resetLoginGUI();
    }

    void resetLoginGUI() {
      feedback.setText("Welcome to the IRC Server!");
    }

    
    /***************************************************************
     * Appends feedback to the feedback display in the login menu.
     * @param message String of feedback to be appended to display
     ***************************************************************/
    private void displayFeedback(String message) {
      feedback.append("\n" + message);
      feedback.setCaretPosition(feedback.getDocument().getLength());
    }

    
    /**************************************************************************************************************
     * On an action event occurring in the login menu, this function is called.
     * @param event ActionEvent object representing an event that has occurred in the login menu
     *************************************************************************************************************/
    public void actionPerformed(ActionEvent event) {
      // start server button pressed
      if (event.getSource() == startButton) {
        String portString = portField.getText();
        String username = usernameField.getText();
        if (portString.equals("") || username.equals("")) {
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
        displayFeedback("Attempting to host server on port " + portString + "...");
        if (!startServer(port, username)) {
          displayFeedback("Unable to host server.");
          return;
        }
        displayFeedback("Success! Server hosted on port " + portString + ".");
        runConnectionListener();
      }
      // clear button pressed
      if (event.getSource() == clearButton) {
        portField.setText("");
        usernameField.setText("");
      }
    }
  }

  /*************************************************************************
   * Main entry point to the program. Starts the application.
   * After the application is started, then the constructor for server().
   * @param args 
   *************************************************************************/
  @SuppressWarnings("unused")
  public static void main(String[] args) {
	Server server = new Server();
  }
}