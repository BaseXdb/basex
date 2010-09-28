import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Language Binding for BaseX.
 * Works with BaseX 6.1.9 and later
 * Documentation: http://basex.org/api
 *
 * (C) Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public class BaseXClient {
  /** Output stream. */
  final OutputStream out;
  /** Socket. */
  private final Socket socket;
  /** Cache. */
  private final BufferedInputStream in;
  /** Command info. */
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
    out = socket.getOutputStream();

    // receive timestamp
    final String ts = receive();
    // send {Username}0 and hashed {Password/Timestamp}0
    send(usern);
    send(md5(md5(pw) + ts));

    // receive success flag
    if(!ok()) throw new IOException("Access denied.");
  }

  /**
   * Executes a command and serializes the result to an output stream.
   * @param cmd command
   * @param o output stream
   * @throws IOException Exception
   */
  public void execute(final String cmd, final OutputStream o)
      throws IOException {
    // send {Command}0
    send(cmd);
    receive(o);
    info = receive();
    if(!ok()) throw new IOException(info);
  }

  /**
   * Executes a command and returns the result.
   * @param cmd command
   * @return result
   * @throws IOException Exception
   */
  public String execute(final String cmd) throws IOException {
    final OutputStream os = new ByteArrayOutputStream();
    execute(cmd, os);
    return os.toString();
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
   * Creates a database.
   * @param name name of database
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void create(final String name, final InputStream input)
      throws IOException {
    // send 3 to mark start of query execution, and {Query}0 as query string
    out.write(3);
    send(name);
    int l;
    while((l = input.read()) != -1) out.write(l);
    out.write(0);
    info = receive();
    if(!ok()) throw new IOException(info);
  }

  /**
   * Returns command information.
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
    out.flush();
    socket.close();
  }

  /**
   * Checks the next success flag.
   * @return value of check
   * @throws IOException Exception
   */
  boolean ok() throws IOException {
    out.flush();
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
    out.write((s + '\0').getBytes("UTF8"));
  }

  /**
   * Receives a string and writes it to the specified output stream.
   * @param o output stream
   * @throws IOException I/O exception
   */
  private void receive(final OutputStream o) throws IOException {
    out.flush();
    while(true) {
      final int b = in.read();
      if(b == 0 || b == -1) break;
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
      // send 0 to mark start of query execution, and {Query}0 as query string
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
      // send 1 to get next result item, and {ID}0 for identification
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
      // send 2 to mark end of query execution, and {ID}0 for identification
      out.write(2);
      send(id);
      out.flush();
    }
  }
}
