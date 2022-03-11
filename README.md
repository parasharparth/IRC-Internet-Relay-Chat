**********************************************************************************************************************************
**README File for Internet Relay Chat (IRC) Protocol implemented as part of project requirement of CS594 (Internetworking Protocols).**
**********************************************************************************************************************************



********************************************************************************************************************************************
Version-1 
Using the synchronous keyword for performing server, packet and client sync
Used the input and output streams of the sockets for transferring information to and fro between server, client and client , client
*********************************************************************************************************************************************

List of Files:
==============

In addition to this README file, the Project submission consists of three files:

1) SocketClient.java : Connects to a server, enters a chat room of its choice in order to exchange messages with other clients.
2) SocketServer.java : Accepts connections from clients, puts a client in the requested chat room
3) ChatRoom.java : Maintains the implementation of a chat room, that is, it adds clients to a chat room and records the name of a chat room.

Instructions for Compiling the program:
======================================

1) Compile the Client:   javac SocketClient.java
2) Compile the Server:   javac SocketServer.java
3) Compile the ChatRoom: javac ChatRoom.java

Instructions for Running the Program:
====================================

1) Start the server: java SocketServer
2) Start the client: java SocketClient (Type this command in a different xterm/terminal). For more clients, open several xterms and type this command.
3) Enter the IP address of the server: 
   a) if client and server are on the same system, then enter "localhost" and click "Ok".
   b) else, enter the IP address of the server (e.g., "10.0.0.5") and click "Ok".
4) Enter a unique client name (e.g., "Parth") and click "Ok". If a unique client name is not entered, the window will prompt to enter a unique name. 
5) Enter the name of the chatRoom the client wishes to join (e.g., "Room1") and click "Ok".
6) Type the messages and press Enter to broadcast in the room.
7) To see the available chatRooms, type "LISTOFCHATROOMS" in the message box and press Enter.
8) To leave a chatRoom, close the window. 

The processes will run in parallel synchronoulsy




************************************************************************************************************************************************
Version-2
Using the serialize interface for performing server, packet and client sync
Used the ObjectInput and ObjectOutput streams of the sockets for transferring information to and fro between server, client and client , client
****************************************************************************************************************************************************

List of Files:
==============

In addition to this README file, the Project submission consists of three files:

1) Client.java : Connects to a server, enters a chat room of its choice in order to exchange messages with other clients.
2) Server.java : Accepts connections from clients, puts a client in the requested chat room
3) Packet.java : Abstract implementation of generic packets

Instructions for Compiling the program:
======================================

1) Compile the Client:   javac Client.java
2) Compile the Server:   javac Server.java
3) Compile the ChatRoom: javac Packet.java

Instructions for Running the Program:
====================================

1) Start the server: java Server
2) The server GUI window will open and then ask the user to connect to the 
3) Start the client: java Client (Type this command in a different xterm/terminal). For more clients, open several xterms and type this command.
4) Enter the IP address of the server: 
   a) if client and server are on the same system, then enter "localhost" and click "Ok".
   b) else, enter the IP address of the server (e.g., "10.0.0.5") and click "Ok".
5) Enter a unique client name (e.g., "Parth") and click "Connect to Server". 
6) The list of rooms is being displayed in the client window and the client can enter/create a room by using @join <room number>/ @create <room number>
7) The client can leave a room using @leave <room number>
8) The list of users is displayed on the client GUI window as well as on the server GUI window
9) Host can remove any client using the @remove <client number>
10) Users can send private messages to each other using the @user <user name> <message> command







