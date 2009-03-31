package org.basex.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Session for a Client Server Connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Session extends Thread {
  
  /** Socket. */
  private Socket socket;
  /** ClientId. */
  private int clientId;
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   */
  public Session(final Socket s, final int c) {
    super("Session");
    socket = s;
    clientId = c;
    System.out.println("Connection on " + socket + " from " + clientId);
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
 // get command and arguments
    final DataInputStream dis = new DataInputStream(socket.getInputStream());
    final String in = dis.readUTF().trim();
    System.out.println(in);
    /*PrintWriter zumClient = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader vomClient = new BufferedReader(new InputStreamReader(
        socket.getInputStream()));
    
    String ein, aus;
    aus = "Hallo hier ist der Echo-Server";
    zumClient.println(aus);

    while ((ein = vomClient.readLine()) != null) {
      zumClient.println(" Vom Server : " + ein);
    }
    vomClient.close();
    zumClient.close();
    socket.close();*/
  }
  
  @Override
  public void run() {
    try {
      handle();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

}
