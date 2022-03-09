package client;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class SocketClient {
    
    String clientName,chatRoomName;
    BufferedReader input1;
    PrintWriter output1;
    JFrame frame = new JFrame("Chat_World");
//    JFrame.setBackground(Color.RED);
    JTextField textField = new JTextField(30);
    JTextArea messageArea = new JTextArea(6, 30);
   
    public SocketClient() {
        
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
//        frame.getContentPane().setBackground(Color.RED);
        frame.pack();
        
 
        textField.addActionListener(new ActionListener() {
            
		// An event occurs when the client presses the enter key after entering some value in the text field.
            public void actionPerformed(ActionEvent e) {
                String textInput = textField.getText();
                
                if(!textInput.startsWith("LISTOFCHATROOMS"))
                {
                    messageArea.append(clientName+ ":" + textInput+"\n");
                }
                
                output1.println( chatRoomName +":"+ textInput);
                textField.setText("");
            }
        });
    }
    
	// Asking the client for server's IP address
    private String getServerAddress() {
        return JOptionPane.showInputDialog(frame,"Please provide the IP Address of the Server you wish to connect to:","Good morning! Happy Chatting",JOptionPane.QUESTION_MESSAGE);
    }
    
    
    // Asking the client for its unique name
    private String getName() {
        return JOptionPane.showInputDialog(frame,"Please provide your unique name:","Client name authentication",JOptionPane.QUESTION_MESSAGE);
    }
    
    // Asking client which chat room it wants to join
    private String getChatRoomName() {
        return JOptionPane.showInputDialog(frame,"Which Chat Room would you like to join:", "Chat Room name selection",JOptionPane.QUESTION_MESSAGE);
    }
    
    // Implementation of socket connection and communication in it
    private void run() throws IOException {
        
        // CLient side socket
        String serverAddress = getServerAddress();
        @SuppressWarnings("resource")
		Socket socket = new Socket(serverAddress, 8080);
        input1 = new BufferedReader(new InputStreamReader(
                                                      socket.getInputStream()));
        output1 = new PrintWriter(socket.getOutputStream(), true);
        
        // Special commands from server are executed
        while (true) { 
            String line = input1.readLine();
            if (line.startsWith("PROVIDEANAME")) {
                clientName = getName();
                output1.println(clientName);
            }else if (line.startsWith("PROVIDEACHATROOMNAME")) {
                chatRoomName = getChatRoomName();
                output1.println(chatRoomName);
            }else if (line.startsWith("UNIQUE_NAME")) {
                textField.setEditable(true);
            } else
            {
                // Detecting messages received from other clients
              if (line.startsWith("MESSAGE")&& !line.split(":")[1].equals(clientName))
                {
                    if(line.split(":")[2].equals(chatRoomName))
                    {
                        messageArea.append(line.split(":")[1] +":"+line.split(":")[3]+ "\n");
                    }
                }
                else if (line.startsWith("LISTOFCHATROOMS"))
                {
                 if(line.split(":")[1].equals(clientName)&&line.split(":")[2].equals(chatRoomName))
                   {
                       messageArea.append("\n ChatRooms :"+line.split(":")[3]+ "\n" );
                }
            }
          }
        }
     
    }
    
    public static void main(String[] args) throws Exception {
        SocketClient client = new SocketClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
