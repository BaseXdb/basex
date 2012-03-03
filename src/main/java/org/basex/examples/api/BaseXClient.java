package org.basex.examples.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Java client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-12, BSD License
 */
public class BaseXClient {
  /** UTF-8 charset. */
  static final Charset UTF8 = Charset.forName("UTF-8");
  /** Event notifications. */
  final Map<String, EventNotifier> notifiers =
    new HashMap<String, EventNotifier>();
  /** Output stream. */
  final OutputStream out;
  /** Socket. */
  final Socket socket;
  /** Cache. */
  final BufferedInputStream in;
  /** Command info. */
  String info;
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
    receive(in, o);
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
    return new String(os.toByteArray(), UTF8);
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
    send(8, name, input);
  }

  /**
   * Adds a document to a database.
   * @param path path to document
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void add(final String path, final InputStream input)
      throws IOException {
    send(9, path, input);
  }

  /**
   * Replaces a document in a database.
   * @param path path to document
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void replace(final String path, final InputStream input)
      throws IOException {
    send(12, path, input);
  }

  /**
   * Stores a binary resource in a database.
   * @param path path to document
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void store(final String path, final InputStream input)
      throws IOException {
    send(13, path, input);
  }

  /**
   * Watches an event.
   * @param name event name
   * @param notifier event notification
   * @throws IOException I/O exception
   */
  public void watch(final String name, final EventNotifier notifier)
      throws IOException {
    out.write(10);
    if(esocket == null) {
      final int eport = Integer.parseInt(receive());
      // initialize event socket
      esocket = new Socket();
      esocket.connect(new InetSocketAddress(ehost, eport), 5000);
      final OutputStream os = esocket.getOutputStream();
      receive(in, os);
      os.write(0);
      os.flush();
      final InputStream is = esocket.getInputStream();
      is.read();
      listen(is);
    }
    send(name);
    info = receive();
    if(!ok()) throw new IOException(info);
    notifiers.put(name, notifier);
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
    receive(in, os);
    return new String(os.toByteArray(), UTF8);
  }

  /**
   * Sends a string to the server.
   * @param s string to be sent
   * @throws IOException I/O exception
   */
  void send(final String s) throws IOException {
    out.write((s + '\0').getBytes(UTF8));
  }

  /**
   * Receives a string and writes it to the specified output stream.
   * @param is input stream
   * @param os output stream
   * @throws IOException I/O exception
   */
  static void receive(final InputStream is, final OutputStream os)
      throws IOException {

    for(int b; (b = is.read()) > 0;) {
      // read next byte if 0xFF is received
      os.write(b == 0xFF ? is.read() : b);
    }
  }

  /**
   * Sends a command, argument, and input.
   * @param cmd command
   * @param path path to document
   * @param input xml input
   * @throws IOException I/O exception
   */
  private void send(final int cmd, final String path, final InputStream input)
      throws IOException {
    out.write(cmd);
    send(path);
    send(input);
  }

  /**
   * Starts the listener thread.
   * @param is input stream
   */
  private void listen(final InputStream is) {
    final BufferedInputStream bi = new BufferedInputStream(is);
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            receive(bi, os);
            final String name = new String(os.toByteArray(), UTF8);
            os = new ByteArrayOutputStream();
            receive(bi, os);
            final String data = new String(os.toByteArray(), UTF8);
            notifiers.get(name).notify(data);
          }
        } catch(final IOException ex) { /* ignored */ }
      }
    }.start();
  }

  /**
   * Sends an input stream to the server.
   * @param input xml input
   * @throws IOException I/O exception
   */
  private void send(final InputStream input) throws IOException {
    final BufferedInputStream bis = new BufferedInputStream(input);
    final BufferedOutputStream bos = new BufferedOutputStream(out);
    for(int b; (b = bis.read()) != -1;) {
      // 0x00 and 0xFF will be prefixed by 0xFF
      if(b == 0x00 || b == 0xFF) bos.write(0xFF);
      bos.write(b);
    }
    bos.write(0);
    bos.flush();
    info = receive();
    if(!ok()) throw new IOException(info);
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
  public class Query {
    /** Query id. */
    private final String id;
    /** Cached results. */
    private ArrayList<byte[]> cache;
    /** Cache pointer. */
    private int pos;

    /**
     * Standard constructor.
     * @param query query string
     * @throws IOException I/O exception
     */
    public Query(final String query) throws IOException {
      id = exec(0, query);
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
      if(cache == null) {
        out.write(4);
        send(id);
        cache = new ArrayList<byte[]>();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        while(in.read() > 0) {
          receive(in, os);
          cache.add(os.toByteArray());
          os.reset();
        }
        if(!ok()) throw new IOException(receive());
      }
      return pos < cache.size();
    }

    /**
     * Returns the next item.
     * @return item string
     * @throws IOException I/O Exception
     */
    public String next() throws IOException {
      return more() ? new String(cache.set(pos++, null), UTF8) : null;
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
     * Returns query info in a string.
     * @return query info
     * @throws IOException I/O exception
     */
    public String info() throws IOException {
      return exec(6, id);
    }

    /**
     * Returns serialization parameters in a string.
     * @return query info
     * @throws IOException I/O exception
     */
    public String options() throws IOException {
      return exec(7, id);
    }

    /**
     * Closes the query.
     * @throws IOException I/O exception
     */
    public void close() throws IOException {
      exec(2, id);
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
