package org.basex.server;

import java.io.*;
import java.net.*;

/**
 * Test Client.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class TestClient {
  
  /**
   * Constructor.
   */
  private TestClient() {
  }
  
  /**
   * Main method of the <code>TestClient</code>, launching a local
   * client instance that sends all commands to a server instance.
   * @param args command-line arguments
   * @throws IOException I/O Exception
   */
  public static void main(final String[] args) throws IOException {
    Socket echoSocket = null;
    PrintWriter os = null;
    BufferedReader is = null;
    String hostname = "localhost";
    try {
      echoSocket = new Socket(hostname, 1984);
      os = new PrintWriter(echoSocket.getOutputStream(), true);
      is = new BufferedReader(new InputStreamReader(
          echoSocket.getInputStream()));
    } catch(UnknownHostException ausnahme) {
      System.err.println("Host : " + hostname + "unknown");
      System.exit(1);
    } catch(IOException fehler) {
      System.err.println("No IO-Connection to" + hostname);
      System.exit(1);
    }

    BufferedReader ein = new BufferedReader(new InputStreamReader(System.in));
    String input;
    String message = is.readLine();
    System.out.println(message);
    
    while((input = ein.readLine()) != null) {
      if(input.equals("exit")) {
        os.println("exit");
        break;
      }
      os.println(input);
      message = is.readLine();
      System.out.println(message);
    }
    os.close();
    is.close();
    echoSocket.close();
  }
}
