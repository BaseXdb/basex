import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Java client for BaseX.
 * Works with BaseX 6.3.1 and later
 * Documentation: http://basex.org/api
 *
 * (C) BaseX Team 2005-11, BSD License
 */
public final class BaseXClient {
  /** Event notifications. */
  final Map<String, EventNotifier> notifiers =
    new HashMap<String, EventNotifier>();
  /** Output stream. */
  final OutputStream out;
  /** Socket. */
  private final Socket socket;
  /** Cache. */
  private final BufferedInputStream in;
  /** Command info. */
  private String info;
  /** Socket event reference. */
  Socket esocket;
  /** Socket host name. */
  String ehost;

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
    ehost = host;
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
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    execute(cmd, os);
    return os.toString("UTF-8");
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
    out.write(8);
    send(name);
    send(input);
  }

  /**
   * Adds a database.
   * @param name name of document
   * @param target target path
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void add(final String name, final String target,
      final InputStream input) throws IOException {
    out.write(9);
    send(name);
    send(target);
    send(input);
  }

  /**
   * Watches an event.
   * @param name event name
   * @param notifier event notification
   * @throws IOException I/O exception
   */
  public void watch(final String name,
      final EventNotifier notifier) throws IOException {
    out.write(10);
    send(name);
    if(esocket == null) {
      final int eport = Integer.parseInt(receive());
      // initialize event socket
      esocket = new Socket();
      esocket.connect(new InetSocketAddress(ehost, eport), 5000);
      final OutputStream os = esocket.getOutputStream();
      receive(in, os);
      os.write(0);
      os.flush();
      listen(esocket.getInputStream());
    }
    info = receive();
    if(!ok()) throw new IOException(info);
    notifiers.put(name, notifier);
  }

  /**
   * Starts the listener thread.
   * @param is input stream
   */
  private void listen(final InputStream is) {
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            final BufferedInputStream bi = new BufferedInputStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            receive(bi, baos);
            final String name = baos.toString("UTF-8");
            baos = new ByteArrayOutputStream();
            receive(bi, baos);
            final String data = baos.toString("UTF-8");
            notifiers.get(name).notify(data);
          }
        } catch(final Exception ex) { }
      }
    }.start();
  }

  /**
   * Unwatches an event.
   * @param name event name
   * @throws IOException I/O exception
   */
  public void unwatch(final String name) throws IOException {
    out.write(11);
    send(name);
    info = receive();
    if(!ok()) throw new IOException(info);
    notifiers.remove(name);
  }

  /**
   * Sends an input stream to the server.
   * @param input xml input
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    int l;
    final BufferedInputStream bis = new BufferedInputStream(input);
    final BufferedOutputStream bos = new BufferedOutputStream(out);
    while((l = bis.read()) != -1) bos.write(l);
    bos.write(0);
    bos.flush();
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
    if(esocket != null) esocket.close();
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
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    receive(os);
    return os.toString("UTF-8");
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
   * @param bis input stream
   * @param o output stream
   * @throws IOException I/O exception
   */
  void receive(final BufferedInputStream bis,
      final OutputStream o) throws IOException {
    while(true) {
      final int b = bis.read();
      if(b == 0 || b == -1) break;
      o.write(b);
    }
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
  public final class Query {
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
      id = exec(0, query);
    }

    /**
     * Initializes the query.
     * @return result header
     * @throws IOException I/O exception
     */
    public String init() throws IOException {
      return exec(4, id);
    }

    /**
     * Binds a variable.
     * @param name name of variable
     * @param value value
     * @throws IOException I/O exception
     */
    public void bind(final String name, final String value)
        throws IOException {
      exec(3, id + '\0' + name + '\0' + value + '\0');
    }

    /**
     * Checks for the next item.
     * @return result of check
     * @throws IOException I/O exception
     */
    public boolean more() throws IOException {
      next = exec(1, id);
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
     * Returns the whole result of the query.
     * @return query result
     * @throws IOException I/O Exception
     */
    public String execute() throws IOException {
      return exec(5, id);
    }

    /**
     * Returns the query info.
     * @return query info
     * @throws IOException I/O exception
     */
    public String info() throws IOException {
      return exec(6, id);
    }

    /**
     * Closes the query.
     * @return result footer
     * @throws IOException I/O exception
     */
    public String close() throws IOException {
      final String s = exec(2, id);
      out.flush();
      return s;
    }

    /**
     * Executes the specified command.
     * @param cmd command
     * @param arg argument
     * @return resulting string
     * @throws IOException I/O exception
     */
    private String exec(final int cmd, final String arg)
        throws IOException {
      out.write(cmd);
      send(arg);
      final String s = receive();
      if(!ok()) throw new IOException(receive());
      return s;
    }
  }

  /**
   * Interface for event notifications.
   */
  public interface EventNotifier {
    /**
     * Invoked when a database event was fired.
     * @param value event string
     */
    void notify(final String value);
  }
}
