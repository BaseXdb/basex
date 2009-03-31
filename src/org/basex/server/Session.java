package org.basex.server;

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
    this.socket = s;
    this.clientId = c;
    System.out.println("Connection on " + socket + " from " + clientId);
  }
  
  @Override
  public void run() {
    
  }

}
