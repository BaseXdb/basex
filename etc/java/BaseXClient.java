import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * For the execution of commands you need to call the execute() method with the
 * database command as argument. The method returns a boolean, indicating if
 * the command was successful. The result is stored in the Result property,
 * and the Info property returns additional processing information or error
 * output.
 *
 * An even faster approach is to call execute() with the database command and
 * an output stream. The result will directly be printed and does not have to
 * be cached.
 *
 * -----------------------------------------------------------------------------
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public class BaseXClient {
  /** Output stream. */
  final OutputStream out;
  /** Socket. */
  private final Socket socket;
  /** Cache. */
  private final BufferedInputStream in;
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
    in = new BufferedInputStream(socket.getInputStream());

    // receive timestamp
    final String ts = receive();

    // send user name and hashed password/timestamp
    out = socket.getOutputStream();
    send(usern);
    send(md5(md5(pw) + ts));

    // receive success flag
    if(!ok()) throw new IOException();
  }

  /**
   * Executes a command and serializes the result to the specified stream.
   * @param cmd command
   * @param o output stream
   * @return boolean success flag
   * @throws IOException Exception
   */
  public boolean execute(final String cmd, final OutputStream o)
      throws IOException {

    send(cmd);
    receive(o);
    info = receive();
    return ok();
  }

  /**
   * Executes a command and returns the result.
   * @param cmd command
   * @return result
   * @throws IOException Exception
   */
  public String execute(final String cmd) throws IOException {
    send(cmd);
    final String s = receive();
    info = receive();
    if(!ok()) throw new IOException(info);
    return s;
  }
  
  /**
   * Creates a query object.
   * @param query query string
   * @return query
   * @throws IOException Exception
   */
  public Query query(final String query) throws IOException {
    return new Query(query);
  }

  /**
   * Returns process information.
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
   * Checks the next success flag.
   * @return value of check
   * @throws IOException Exception
   */
  boolean ok() throws IOException {
    return in.read() == 0;
  }

  /**
   * Returns the next received string.
   * @return String result or info
   * @throws IOException I/O exception
   */
  String receive() throws IOException {
    final OutputStream os = new ByteArrayOutputStream();
    receive(os);
    return os.toString();
  }

  /**
   * Sends a string to the server.
   * @param s string to be sent
   * @throws IOException I/O exception
   */
  void send(final String s) throws IOException {
    for(final byte t : s.getBytes()) out.write(t);
    out.write(0);
  }
  
  /**
   * Receives a string and writes it to the specified output stream.
   * @param o output stream
   * @throws IOException I/O exception
   */
  private void receive(final OutputStream o) throws IOException {
    while(true) {
      final int b = in.read();
      if(b == 0) break;
      o.write(b);
    }
  }

  /**
   * Returns an MD5 hash.
   * @param pw String
   * @return String
   */
  private static String md5(final String pw) {
    final StringBuilder sb = new StringBuilder();
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(pw.getBytes());
      for(final byte b : md.digest()) {
        final String s = Integer.toHexString(b & 0xFF);
        if(s.length() == 1) sb.append('0');
        sb.append(s);
      }
    } catch(final NoSuchAlgorithmException ex) {
      // should not occur
      ex.printStackTrace();
    }
    return sb.toString();
  }
  
  /**
   * Inner class for iterative query execution.
   */
  class Query {
    /** Query id. */
    private final String id;
    /** Next result item. */
    private String next;
    
    /**
     * Standard constructor.
     * @param query query string
     * @throws IOException I/O exception
     */
    public Query(final String query) throws IOException {
      out.write(0);
      send(query);
      id = receive();
      if(!ok()) throw new IOException(receive());
    }
    
    /**
     * Checks for the next item.
     * @return value of check
     * @throws IOException I/O exception
     */
    public boolean more() throws IOException {
      out.write(1);
      send(id);
      next = receive();
      if(!ok()) throw new IOException(receive());
      return next.length() != 0;
    }
    
    /**
     * Returns the next item.
     * @return item string
     */
    public String next() {
      return next;
    }

    /**
     * Closes the query.
     * @throws IOException I/O exception
     */
    public void close() throws IOException {
      out.write(2);
      send(id);
    }
  }
}
