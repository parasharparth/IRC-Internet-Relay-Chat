package server;

import chatroom.*;

import java.awt.*;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import javax.swing.*;


/***********************************************************************************************
 This Internet Relay Chat is designed by using threads and java swings concepts. 
 A Client chats with other clients by choosing a chat room. 
 Every client will have a unique name and every chat room will also have a unique name.
***********************************************************************************************/ 

public class SocketServer extends JFrame implements ActionListener{ 

	/******************************************************************************************
	 *Generated the Default Serial Version for the serialize interface 
	 *Serialize interface is internally used by the socket class for transmission of the data
	 *****************************************************************************************/
	private static final long serialVersionUID = 1L;

	// The server will listen on port number 8080 , This also needs to be made configurable along with IP address
	//Also create error codes for the errors so that proper message can be displayed whenever something goes wrong in the exit window
    
	public static String PORT = "";
	
	/********************************************************
	 * JTextField
	 * JFrame
	 * JButton 
	 * This is for getting port number from the user
	 *******************************************************/
    static JTextField t;
    static JFrame f;
    static JButton b;
    
    
    /************************************************************************************************************************
     * These HashSets are present for the following reasons
     * client_names:- HashSet maintains record of all unique names of clients within a chat room.This eliminated duplicates.
     * writers:- For easy broadcast of messages, hashSet is used to maintain a record for all writers of all clients.
     * chatRooms:- HashSet to maintain a record for all the available chat rooms
     ************************************************************************************************************************/
    private static HashSet<String> client_names = new HashSet<String>(); 
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); 
    private static HashSet<ChatRoom> chatRooms = new HashSet<ChatRoom>(); 
    
    
    
    /***********************************************
     * Main Method
     * This is the starting point of the program
     * @param args
     * @throws Exception
     **********************************************/
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
    	
    	/****************************************************************************
    	 * The First step is to get the port number from the user
    	 * create a new frame to store text field and button
    	 * create a new button
    	 * create a object of the SocketServer class
    	 * addActionListener to button
    	 * create a object of JTextField with 16 columns and a given initial text
    	 * create a panel to add buttons and textField
    	 ****************************************************************************/
        f = new JFrame("textfield");
        b = new JButton("submit");
        SocketServer te = new SocketServer();
        b.addActionListener(te);
        t = new JTextField(" ", 16);
        JPanel p = new JPanel();
        p.add(t);
        p.add(b);
        f.add(p);
        f.setSize(300, 300);
        f.show();
        Thread.sleep(10000);
    
    /********************************
     * Message for the programmer
     ********************************/
    System.out.println("The Internet Relay Chat has its server side running.");
    
    
    /*********************************************************
     * Server is listening for clients sockets requests
     *********************************************************/
    int port = Integer.parseInt(PORT);
   	ServerSocket listener = new ServerSocket(port);
   
    
    
    /*************************************************************************************************
     * This will be used to print the number of clients connected with the server at a particular time 
     *************************************************************************************************/
    @SuppressWarnings("unused")
	int numberOfClients = 0;   
    
    
    /****************************************************************************************************
     * After getting the port number from the user, we have established a socket which is in the
     * listening state waiting to receive a connection from the client
     * 
     * Here, we will call the listener.accept() method which will start listening to the client 
     * The listener.accept().start() will start a new thread for this new client
     * numberOfClients++ will increment the number of clients present
     * 
     * All this is being done by using the handler
     ****************************************************************************************************/
        try {
            while (true) { 
                new Handler(listener.accept()).start(); 
                numberOfClients++; 
            }
        } 
        catch(Exception e)
        {
        	System.out.println("Exception occured"); 
        	System.exit(1);
        }
        finally {
            listener.close();
            numberOfClients--; 
        }
    }
    
    
    /************************************************************************************************
     * This method is used as an actionEvent listener for getting the port number from the user
     * @param e:- ActionEvent
     ***********************************************************************************************/
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        if (s.equals("submit")) {
            PORT =  t.getText().strip();
            System.out.println(PORT);
        }
    }
    
    
    /*****************************************************************************************************
     * This is the handler where new threads (clients) are connected to the 
     *
     ****************************************************************************************************/
    private static class Handler extends Thread
    {
        private String name;
        @SuppressWarnings("unused")
		private String chatRoomName;
        private Socket socket;
        private BufferedReader input1;
        private PrintWriter output1;
        
        public Handler(Socket socket)
        {
            this.socket = socket;
        }
        
       @SuppressWarnings({ "unused", "unlikely-arg-type" })
	public int checkIfRoomExists(String roomName)
        {
            
            Iterator<ChatRoom> iterator = chatRooms.iterator(); // new iterator to pass through the data record
            
            while (iterator.hasNext()){ 
               if(iterator.next().equals(roomName))
               {
                   return 1;
               }
            }
            return 0;
        }
        
        // Gets the chat room object based on input room name
       public ChatRoom getChatRoom(String roomName)
        {
            Iterator<ChatRoom> iterator = chatRooms.iterator();
            
            while (iterator.hasNext()){
                output1.println("check_chatroom");
                ChatRoom room = (ChatRoom)iterator.next();
                if(room.name.equals(roomName))
                {
                    return room;
                }
            }
            return null;
        }
        
        // gets the list of chat rooms
        public String getChatRoomsList()
        { 
            String chatRooomsList = "";   
            Iterator<ChatRoom> iterator = chatRooms.iterator();
            
            while (iterator.hasNext())
            {
                ChatRoom room = (ChatRoom)iterator.next();
                chatRooomsList += room.name + ",";
            }
            return chatRooomsList;
        }

        public void run()
        {
            try
            { 
                input1 = new BufferedReader(new InputStreamReader(socket.getInputStream())); //input stream for the socket
                output1 = new PrintWriter(socket.getOutputStream(), true); // output stream for the socket
                
                // Client's names are checked if they are unique
                while (true) 
                {
                    output1.println("PROVIDEANAME");
                    name = input1.readLine();
                    if (name == null) 
                    {
                        return;
                    }
                    synchronized (client_names) 
                    {
                        if (!client_names.contains(name)) 
                        {
                            client_names.add(name);
                        }
                        else
                        {
                        	output1.println("PROVIDEANAME");	
                        }
                    }
                    
                    output1.println("PROVIDEACHATROOMNAME");
                    
                    String  chatRoomNameInput = input1.readLine();
                    if (chatRoomNameInput == null) 
                    {
                        return;
                    }
                    synchronized (chatRooms) 
                    {    
                        ChatRoom room = getChatRoom(chatRoomNameInput);
                        if(room == null)
                         {
                              room = new ChatRoom();
                              room.setName(chatRoomNameInput);
                             chatRooms.add(room);
                         }
                        ChatRoom.writers.add(output1);
                        break;
                    }
                }
                output1.println("UNIQUE_NAME");
                
		// broadcast of messages by a client in a chat room behind
                while (true) 
                {
                    String input = input1.readLine();
                    if (input == null) 
                    {
                        return;
                    }
                    
                    String chatRoomName = input.split(":")[0];
                    
                    String messageInput = input.split(":")[1];
                    
                    @SuppressWarnings("unused")
					ChatRoom room = getChatRoom(chatRoomName);
                    
                        for (PrintWriter writer :ChatRoom.writers)
                        {
                            if(messageInput.startsWith("LISTOFCHATROOMS"))
                            {
                                writer.println("LISTOFCHATROOMS:" + name + ":" +chatRoomName+":"+ getChatRoomsList());
                            }
                            else
                            {
                              writer.println("MESSAGE:" + name + ":" +chatRoomName+":"+ messageInput);
                            }
                        }
                    
                }
            }
            catch (IOException e)
            {
            	System.out.println("Error occured, Kindly check the connection with the client or restart the server");
            	createErrorWindow();
               // System.out.println(e);
            }
            finally
            {
                  if (name != null) {
                   client_names.remove(name); // remove the client name when leaves
                }
                if (output1 != null) {
                    writers.remove(output1); // remove the client print writer
                }
                try {
                    socket.close(); // close the socket
                } catch (IOException e)
                {
                	System.out.println("Something went wrong, try again later");
                	createErrorWindow();
                }
            }
        }
        private static void createErrorWindow() {    
            JFrame frame = new JFrame("Error Window");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            createUI(frame);
            frame.setSize(360, 100);      
            frame.setLocationRelativeTo(null);  
            frame.setVisible(true);
         }

         private static void createUI(final JFrame frame){  
            JPanel panel = new JPanel();
            LayoutManager layout = new FlowLayout();  
            panel.setLayout(layout);       
            JButton button = new JButton("Exit");
            button.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e) {
                  JOptionPane.showMessageDialog(frame, "Something went wrong, try again later",
                     "Error Window", JOptionPane.ERROR_MESSAGE);
               }
            });

            panel.add(button);
            frame.getContentPane().add(panel, BorderLayout.CENTER);    
         }  
    }


}


