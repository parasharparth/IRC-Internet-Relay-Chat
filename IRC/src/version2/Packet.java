package version2;

import java.io.Serializable;

 
/**************************************************************************************************************
 * Represents the object that is passed between the server and client. 
 * The packet object contains the information that needs to be parsed by both the client and the server.
 **************************************************************************************************************/
class Packet implements Serializable {
	
/**********************************************************************************************
* Serial version Id for proper identification of the packets in the ObjectInput/outputStream
**************************************************************************************************/
    private static final long serialVersionUID = 1L;
  
    
   /***********************************************************************************************
    * Data Members for the Packet are:-
    * command:-Command received from the client (create a room, send a message to a room, etc.)
    * targetid:- Identifies the target user or room a client wants to send a message to
    * message:- Contents of the message a client wants to send
    **********************************************************************************************/
  String command;
  int targetid; 
  String message;

  
  /**************************************************************************************************
   * Returns the command, target-id, and message fields to null values. 
   * This ensures a clean slate for the incoming command.
   **************************************************************************************************/
  private void clear() {
    command = null;
    targetid = -1;
    message = null;
  }

  
  /************************************************************************************************************************************
   * This method is called when a client initiates a connection with the server.
   *  The client's unique user-name is passed in as the message parameter.
   * <p>This method is also called from the server side once the connection is established.
   *  A welcome message containing the client's unique user-name and identification number is passed in as the message parameter.
   * @param message contains information from the server or client on startup
   ************************************************************************************************************************************/
  void joinServer(String message) {
    clear();
    command = "joinServer";
    this.message = message;
  }

  
  /********************************************************************************************
   * Called when the client disconnects from the server.
   * It is important for the graceful exit of the client and server and avoiding exceptions
   *******************************************************************************************/
  void leaveServer() {
    clear();
    command = "leaveServer";
  }

  
  /*******************************************************************************************
   * When a client connects to the server, or leaves the server, the user list is updated.
   * @param message contains the user id and user-name
   *******************************************************************************************/
  void userUpdate(String message) {
    clear();
    command = "userUpdate";
    this.message = message;
  }

  
  /********************************************************************************************************
   * When a room is created, the room list is updated.
   * @param message contains the room name, room id, and the user id, user-name of connected clients
   ********************************************************************************************************/
  void roomUpdate(String message) {
    clear();
    command = "roomUpdate";
    this.message = message;
  }

  
/**********************************************************************************************************
   * When no commands are present in the user input, a message is broadcast to all connected users.
   * @param message user input containing the contents of the message they wish to send
**********************************************************************************************************/
  void sendMessageAll(String message) {
    clear();
    command = "sendMessageAll";
    this.message = message;
  }

  
  /***************************************************************************************************************
   * This method is called when a client wishes to send a message to a specific user connected to the server. 
   * The client may do this by specifying the users's id in the command, along with their intended message.
   * @param targetid the user id for the user the client wishes to send a message to
   * @param message user input containing the contents of the message they wish to send
   ***************************************************************************************************************/
  void sendMessageUser(int targetid, String message) {
    clear();
    command = "sendMessageUser";
    this.targetid = targetid;
    this.message = message;
  }

  
  /****************************************************************************************************************
   * This method is called when a client wishes to send a message to all users connected to a specific room.
   * The client may do this by specifying the room id in the command, along with their intended message.
   * @param targetid the room id for the room the client wishes to send a message to
   * @param message user input containing the contents of the message they wish to send
   ****************************************************************************************************************/
  void sendMessageRoom(int targetid, String message) {
    clear();
    command = "sendMessageRoom";
    this.targetid = targetid;
    this.message = message;
  }

  
  /*********************************************************************************************************
   * This method is called when a client wishes to create a new "virtual" room.
   * They must supply a room name to create a room.
   * @param message the room name the client wants to create
   ***********************************************************************************************************/
  void createRoom(String message) {
    clear();
    command = "createRoom";
    this.message = message;
  }

  
  /*********************************************************************************************************
   * This method is called when a client wishes to join a room. 
   * They must supply a room id corresponding to the room they wish to join.
   * @param targetid the room id for the room the client wants to join
   ********************************************************************************************************/
  void joinRoom(int targetid) {
    clear();
    command = "joinRoom";
    this.targetid = targetid;
  }

  
  /*********************************************************************************************************
   * This method is called when a client wishes to leave a room. 
   * They must supply a room id corresponding to the room they wish to leave.
   * @param targetid the room id for the room the client wants to leave
   ********************************************************************************************************/
  void leaveRoom(int targetid) {
    clear();
    command = "leaveRoom";
    this.targetid = targetid;
  }

  
  /*********************************************************************************************************
   * Controls the output displayed to the client.
   * @param message contains the full text to display to the screen
   ********************************************************************************************************/
  void displayToUser(String message) {
    clear();
    command = "displayToUser";
    this.message = message;
  }

  
  /********************************************************************************************************* 
   * Shuts down the server, thus closing the application. 
   *********************************************************************************************************/
  void shutdown() {
    clear();
    command = "shutdown";
  }
}