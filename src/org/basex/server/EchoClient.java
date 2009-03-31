package org.basex.server;

import java.io.*;
import java.net.*;

/**
 * EchoClient.
 * @author Andy
 *
 */
public final class EchoClient {
  
  /** boolean. */
  private boolean verbose;
  /**
   * Constructor.
   */
  private EchoClient() {
    System.out.println(this.verbose);
  }
  
  /**
   * Main method.
   * @param args Args
   * @throws IOException I/O Exception
   */
  public static void main(final String[] args) throws IOException {
    Socket echoSocket = null;
    PrintWriter zumServer = null;
    BufferedReader vomServer = null;
    String hostname = "localhost";
    try {
      echoSocket = new Socket(hostname, 1984);
      zumServer = new PrintWriter(echoSocket.getOutputStream(), true);
      vomServer = new BufferedReader(new InputStreamReader(
          echoSocket.getInputStream()));
    } catch(UnknownHostException ausnahme) {
      System.err.println("Host : " + hostname + "unbekannt");
      System.exit(1);
    } catch(IOException fehler) {
      System.err.println("Kann keine IO-Verbindung zu" + hostname
          + "herstellen");
      System.exit(1);
    }

    BufferedReader ein = new BufferedReader(new InputStreamReader(System.in));
    String tastatureingabe;
    String nachricht = vomServer.readLine();
    System.out.println(nachricht);
    while(((tastatureingabe = ein.readLine()) != null)
        && (!tastatureingabe.equals("bye"))) {
      zumServer.println(tastatureingabe);
      nachricht = vomServer.readLine();
      System.out.println("Echo :" + nachricht);
    }
    if(tastatureingabe.equals("bye")) zumServer.println("bye");
    zumServer.close();
    vomServer.close();
    echoSocket.close();
  }
}
