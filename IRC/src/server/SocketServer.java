package server;

import chatroom.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/*
 An Internet Relay Chat is designed using threads and java swings concepts. A Client chats with other clients by choosing a chat room. Every client will have a unique nam e and every chat room will also have a unique name.
 */ 
public class SocketServer {

    
     // The server will listen on port number 8080
 
    private static final int PORT = 8080;
    

    private static HashSet<String> client_names = new HashSet<String>(); //Hashset maintains record of all unique names of clients within a chat room.This eliminated duplicates.

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); 
//For easy broadcast of messages, hashset is used to maintain a record for all writers of all clients.
    
    private static HashSet<ChatRoom> chatRooms = new HashSet<ChatRoom>(); // Hashset to maintian a record for all the available chat rooms
    
    public static void main(String[] args) throws Exception {
        System.out.println("The Internet Relay Chat has its server side running.");
        ServerSocket listener = new ServerSocket(PORT); // Server is listening for clients sockets requests
        try {
            while (true) {
                new Handler(listener.accept()).start(); // it is calling the handler class
            }
        } 
        catch(Exception e)
        {
        	System.out.println("Exception occured"); 
        	System.exit(1);
        }
        finally {
            listener.close();
        }
    }

    // Every connection between a client and a server will have a dedicated handler class

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
                while (true) {
                    output1.println("PROVIDEANAME");
                    name = input1.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (client_names) {
                        if (!client_names.contains(name)) {
                            client_names.add(name);
			
                                                   }
                        else
			{
				
				output1.println("PROVIDEANAME");
				
			}
			}
                    
                    output1.println("PROVIDEACHATROOMNAME");
                    String  chatRoomNameInput = input1.readLine();
                    
                    if (chatRoomNameInput == null) {
                        return;
                    }
                    synchronized (chatRooms) {
                        
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
                
		// broadcast of messaged by a client in a chat room behind
                while (true) {
                    String input = input1.readLine();
                    if (input == null) {
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
                System.out.println(e);
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
                }
            }
        }
    }


}


