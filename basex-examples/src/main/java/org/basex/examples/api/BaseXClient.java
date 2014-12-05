package org.basex.examples.api;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

/**
 * Java client for BaseX.
 * Works with BaseX 7.0 and later
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * (C) BaseX Team 2005-14, BSD License
 */
public final class BaseXClient {
  /** UTF-8 charset. */
  private static final Charset UTF8 = Charset.forName("UTF-8");
  /** Event notifications. */
  private final Map<String, EventNotifier> notifiers =
      Collections.synchronizedMap(new HashMap<String, EventNotifier>());
  /** Output stream. */
  private final OutputStream out;
  /** Input stream (buffered). */
  private final BufferedInputStream in;

  /** Socket. */
  private final Socket socket;
  /** Command info. */
  private String info;
  /** Socket event reference. */
  private Socket esocket;
  /** Socket event host. */
  private final String ehost;

  /**
   * Constructor.
   * @param host server name
   * @param port server port
   * @param username user name
   * @param password password
   * @throws IOException Exception
   */
  public BaseXClient(final String host, final int port, final String username,
      final String password) throws IOException {

    socket = new Socket();
    socket.connect(new InetSocketAddress(host, port), 5000);
    in = new BufferedInputStream(socket.getInputStream());
    out = socket.getOutputStream();
    ehost = host;

    // receive server response
    final String[] response = receive().split(":");
    final String code, nonce;
    if(response.length > 1) {
      // support for digest authentication
      code = username + ':' + response[0] + ':' + password;
      nonce = response[1];
    } else {
      // support for cram-md5 (Version < 8.0)
      code = password;
      nonce = response[0];
    }

    send(username);
    send(md5(md5(code) + nonce));

    // receive success flag
    if(!ok()) throw new IOException("Access denied.");
  }

  /**
   * Executes a command and serializes the result to an output stream.
   * @param command command
   * @param output output stream
   * @throws IOException Exception
   */
  public void execute(final String command, final OutputStream output) throws IOException {
    // send {Command}0
    send(command);
    receive(in, output);
    info = receive();
    if(!ok()) throw new IOException(info);
  }

  /**
   * Executes a command and returns the result.
   * @param command command
   * @return result
   * @throws IOException Exception
   */
  public String execute(final String command) throws IOException {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    execute(command, os);
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
  public void create(final String name, final InputStream input) throws IOException {
    send(8, name, input);
  }

  /**
   * Adds a document to a database.
   * @param path path to resource
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void add(final String path, final InputStream input) throws IOException {
    send(9, path, input);
  }

  /**
   * Replaces a document in a database.
   * @param path path to resource
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void replace(final String path, final InputStream input) throws IOException {
    send(12, path, input);
  }

  /**
   * Stores a binary resource in a database.
   * @param path path to resource
   * @param input xml input
   * @throws IOException I/O exception
   */
  public void store(final String path, final InputStream input) throws IOException {
    send(13, path, input);
  }

  /**
   * Watches an event.
   * @param name event name
   * @param notifier event notification
   * @throws IOException I/O exception
   */
  public void watch(final String name, final EventNotifier notifier) throws IOException {
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
  private boolean ok() throws IOException {
    out.flush();
    return in.read() == 0;
  }

  /**
   * Returns the next received string.
   * @return String result or info
   * @throws IOException I/O exception
   */
  private String receive() throws IOException {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    receive(in, os);
    return new String(os.toByteArray(), UTF8);
  }

  /**
   * Sends a string to the server.
   * @param string string to be sent
   * @throws IOException I/O exception
   */
  private void send(final String string) throws IOException {
    out.write((string + '\0').getBytes(UTF8));
  }

  /**
   * Receives a string and writes it to the specified output stream.
   * @param input input stream
   * @param output output stream
   * @throws IOException I/O exception
   */
  private static void receive(final InputStream input, final OutputStream output)
      throws IOException {
    for(int b; (b = input.read()) > 0;) {
      // read next byte if 0xFF is received
      output.write(b == 0xFF ? input.read() : b);
    }
  }

  /**
   * Sends a command, argument, and input.
   * @param code command code
   * @param path name, or path to resource
   * @param input xml input
   * @throws IOException I/O exception
   */
  private void send(final int code, final String path, final InputStream input) throws IOException {
    out.write(code);
    send(path);
    send(input);
  }

  /**
   * Starts the listener thread.
   * @param input input stream
   */
  private void listen(final InputStream input) {
    final BufferedInputStream bis = new BufferedInputStream(input);
    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            receive(bis, baos);
            final String name = new String(baos.toByteArray(), UTF8);
            baos = new ByteArrayOutputStream();
            receive(bis, baos);
            final String data = new String(baos.toByteArray(), UTF8);
            notifiers.get(name).notify(data);
          }
        } catch(final IOException ex) {
          // loop will be quit if no data can be received anymore
        }
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
    Query(final String query) throws IOException {
      id = exec(0, query);
    }

    /**
     * Binds a value to an external variable.
     * @param name name of variable
     * @param value value
     * @throws IOException I/O exception
     */
    public void bind(final String name, final String value) throws IOException {
      bind(name, value, "");
    }

    /**
     * Binds a value with the specified type to an external variable.
     * @param name name of variable
     * @param value value
     * @param type type (can be an empty string)
     * @throws IOException I/O exception
     */
    public void bind(final String name, final String value, final String type) throws IOException {
      cache = null;
      exec(3, id + '\0' + name + '\0' + value + '\0' + type);
    }

    /**
     * Binds a value to the context item.
     * @param value value
     * @throws IOException I/O exception
     */
    public void context(final String value) throws IOException {
      context(value, "");
    }

    /**
     * Binds a value with the specified type to the context item.
     * @param value value
     * @param type type (can be an empty string)
     * @throws IOException I/O exception
     */
    public void context(final String value, final String type) throws IOException {
      cache = null;
      exec(14, id + '\0' + value + '\0' + type);
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
        cache = new ArrayList<>();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        while(in.read() > 0) {
          receive(in, os);
          cache.add(os.toByteArray());
          os.reset();
        }
        if(!ok()) throw new IOException(receive());
        pos = 0;
      }
      if(pos < cache.size()) return true;
      cache = null;
      return false;
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
     * @param code command code
     * @param arg argument
     * @return resulting string
     * @throws IOException I/O exception
     */
    private String exec(final int code, final String arg) throws IOException {
      out.write(code);
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