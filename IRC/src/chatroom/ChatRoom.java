package chatroom;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatRoom
{
    public String name;
    
    public static HashSet<String> clients = new HashSet<String>(); // Hashset to keep a record of all unique clients so that duplicate clients are not created
    
    public static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); // Hashset to keep a record of all writers for unique clients for easy broadcast in chatroom.
    
    public void addParticipant(String clientName)
    {
        synchronized (clients) {
            if (!clients.contains(clientName))
            {
                clients.add(clientName);
            }
        }

    }
    
    public void setName(String chatRoomName)
    {
        name = chatRoomName;
    }

}
