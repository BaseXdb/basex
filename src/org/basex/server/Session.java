package org.basex.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.basex.core.Context;

/**
 * Session for a Client Server Connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Session extends Thread {
  
  /** Database Context. */
  final Context context = new Context();
  /** Socket. */
  private Socket socket;
  /** ClientId. */
  private int clientId;
  /** Verbose mode. */
  boolean verbose = false;
  /** Flag for server activity. */
  boolean running = true;
  
  /**
   * Session.
   * @param s Socket
   * @param c ClientId
   */
  public Session(final Socket s, final int c) {
    super("Session");
    this.socket = s;
    this.clientId = c;
  }
  
  /**
   * Handles Client Server Communication.
   * @throws IOException I/O Exception
   */
  private void handle() throws IOException {
    System.out.println("Login from Client " + clientId);
    PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
    BufferedReader is = new BufferedReader(new InputStreamReader(
        socket.getInputStream()));

    String in, out;
    out = "You are logged in to the BaseXServer";
    os.println(out);

    while ((in = is.readLine()) != null) {
      if(in.equals("exit")) {
        System.out.println("Client " + clientId + " has logged out.");
        break;
      }
      os.println("Echo from Server: " + in);
    }
    is.close();
    os.close();
    socket.close();
  }
  
  @Override
  public void run() {
    try {
      handle();
    } catch(IOException e) {
      System.out.println("IO-Error");
    }
  }
}
