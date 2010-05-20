package org.basex;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;

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
  
  /** Socket. */
  private Socket socket;
  /** Output stream. */
  final OutputStream out;
  /** Result string. */
  String result;
  /** Input stream. */
  private final InputStream in;
  /** Process info. */
  private String info;
  /** Cache. */
  private byte[] cache = new byte[4096];
  /** Position in cache. */
  private int bpos;
  /** Cache size. */
  private int bsize;
  
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
    final String ts = readString();

    // send user name and hashed password/timestamp
    out = socket.getOutputStream();
    send(usern);
    send(md5(md5(pw) + ts));
    out.flush();

    // receive success flag
    if(in.read() != 0) throw new IOException();
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
    init();
    readString(o);
    info = readString();
    return read() == 0;
  }
  
  /**
   * Executes the command.
   * @param cmd command
   * @return boolean success flag
   * @throws IOException Exception
   */
  public boolean execute(final String cmd) throws IOException {
    send(cmd);
    init();
    result = readString();
    info = readString();
    return read() == 0;
  }
  
  /**
   * Initializes the input read.
   */
  private void init() {
    bpos = 0;
    bsize = 0;
  }
  
  /**
   * Reads input stream.
   * @return byte 1 byte
   * @throws IOException Exception
   */
  private byte read() throws IOException {
    if(bpos == bsize) {
      bsize = in.read(cache);
      bpos = 0;
    }
    return cache[bpos++];
  }
  
  /**
   * Reads string from buffer.
   * @param o output stream
   * @throws IOException Exception
   */
  private void readString(final OutputStream o) throws IOException {
    while(true) {
      byte b = read();
      if(b == 0) break;
      o.write(b);
    }
  }
  
  /**
   * Reads string from buffer.
   * @throws IOException Exception
   * @return String result or info
   */
  private String readString() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    DataOutputStream dos = new DataOutputStream(baos);
    readString(dos);
    return baos.toString();
  }
  
  /**
   * Sends a string to the server.
   * @param s string to be sent
   * @throws IOException I/O exception
   */
  private void send(final String s) throws IOException {
    byte[] sb = s.getBytes();
    for(final byte t : sb) out.write(t);
    out.write(0);
  }
  
  /**
   * Returns the info string.
   * @return string info
   */
  public String info() {
    return info;
  }
  
  /**
   * Returns the result string.
   * @return string result
   */
  public String result() {
    return result;
  }
  
  /**
   * Returns a md5 hash.
   * @param pw String
   * @return String
   */
  public static String md5(final String pw) {
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(pw.getBytes());
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      DataOutputStream dos = new DataOutputStream(baos);
      for(final byte b : md.digest()) {
        final int h = b >> 4 & 0x0F;
        dos.write((byte) (h + (h > 9 ? 0x57 : 0x30)));
        final int l = b & 0x0F;
        dos.write((byte) (l + (l > 9 ? 0x57 : 0x30)));
      }
      return baos.toString();
    } catch(final Exception ex) {
      return pw;
    }
  }
  
  /**
   * Closes the session.
   * @throws IOException Exception
   */
  public void close() throws IOException {
    send("exit");
    socket.close();
  }
}
