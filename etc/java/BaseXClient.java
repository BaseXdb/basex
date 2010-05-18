package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import main.util.BufferInput;
import main.util.LoginException;
import main.util.PrintOutput;
import main.util.Token;


/**
 * -----------------------------------------------------------------------------
 * 
 * This Java module provides methods to connect to and communicate with the
 * BaseX Server.
 *
 * The Constructor of the class expects a hostname, port, username and password
 * for the connection. The socket connection will then be established via the
 * hostname and the port.
 *
 * For the execution of commands you need to call the Execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result is stored in the Result property,
 * and the Info property returns additional processing information or error
 * output.
 *
 * An even faster approach is to call Execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
 * 
 * -----------------------------------------------------------------------------
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class BaseXClient {
  
  /** Socket. */
  private Socket socket;
  /** Output stream. */
  private final PrintOutput out;
  /** Input stream. */
  private final InputStream in;
  /** Process info. */
  private String info;
  
  /**
   * Constructor.
   * @param host server name
   * @param port server port
   * @param usern user name
   * @param pw password
   * @throws IOException Exception
   */
  public BaseXClient(final String host, final int port, final String usern,
      final String pw) throws IOException {
    socket = new Socket();
    socket.connect(new InetSocketAddress(host, port), 5000);
    in = socket.getInputStream();
    
    // receive timestamp
    final String ts = new BufferInput(in).readString();
    
    // send user name and hashed password/timestamp
    out = new PrintOutput(socket.getOutputStream());
    send(usern);
    send(Token.md5(Token.md5(pw) + ts));
    out.flush();

    // receive success flag
    if(in.read() != 0) throw new LoginException();
  }
  
  /**
   * Executes the command.
   * @param cmd command
   * @param o output stream
   * @return boolean success flag
   * @throws IOException Exception
   */
  public boolean execute(final String cmd, final OutputStream o)
  throws IOException {
    send(cmd);
    final BufferInput bi = new BufferInput(in);
    int l;
    while((l = bi.read()) != 0) o.write(l);
    info = bi.readString();
    return bi.read() == 0;
  }
  
  /**
   * Returns the info string.
   * @return string info
   */
  public String info() {
    return info;
  }
  
  /**
   * Closes the session.
   * @throws IOException Exception
   */
  public void close() throws IOException {
    send("exit");
    socket.close();
  }
  
  /**
   * Sends a string to the server.
   * @param s string to be sent
   * @throws IOException I/O exception
   */
  private void send(final String s) throws IOException {
    out.print(s);
    out.write(0);
  }
}
