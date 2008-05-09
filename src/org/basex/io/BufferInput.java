package org.basex.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.basex.util.Token;

/**
 * This class serves as a buffered wrapper for input streams.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BufferInput {
  /** Byte buffer. */
  protected byte[] buffer;
  /** Current buffer position. */
  protected int pos;
  /** Input length. */
  protected long length;
  /** Current buffer size. */
  private int size;
  /** Number of read bytes. */
  private int len;
  /** Reference to the data input stream. */
  private InputStream in;
  /** Default encoding for text files. */
  private String encoding = Token.UTF8;

  /**
   * Empty constructor.
   */
  protected BufferInput() { }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public BufferInput(final String file) throws IOException {
    this(new File(file));
  }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public BufferInput(final File file) throws IOException {
    this(new FileInputStream(file));
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   */
  public BufferInput(final InputStream is) {
    this(is, new byte[4096]);
  }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @param buf input buffer
   * @throws IOException IO Exception
   */
  public BufferInput(final String file, final byte[] buf) throws IOException {
    this(new FileInputStream(file), buf);
    length = file.length();
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   * @param buf input buffer
   */
  public BufferInput(final InputStream is, final byte[] buf) {
    this(buf);
    in = is;
    next();
  }
  
  /**
   * Determines the file encoding.
   */
  public void encoding() {
    final byte a = length > 0 ? buffer[0] : 0;
    final byte b = length > 1 ? buffer[1] : 0;
    final byte c = length > 2 ? buffer[2] : 0;
    final byte d = length > 3 ? buffer[3] : 0;
    if(a == -1 && b == -2 || a == '<' && b == 0 && c == '?' && d == 0) {
      encoding = Token.UTF16LE;
      if(a == -1) pos = 2;
    } else if(a == -2 && b == -1 || a == 0 && b == '<' && c == 0 && d == '?') {
      encoding = Token.UTF16BE;
      if(a == -2) pos = 2;
    } else if(a == -0x11 && b == -0x45 && c == -0x41) {
      pos = 3;
    }
  }

  /**
   * Empty constructor.
   * @param buf buffer
   */
  public BufferInput(final byte[] buf) {
    buffer = buf;
    length = buf.length;
  }
  
  /**
   * Reads a single byte and returns it as integer.
   * @return read byte
   */
  public final int read() {
    return readByte() & 0xFF;
  }
  
  /**
   * Returns the next byte.
   * @return next byte
   */
  public byte readByte() {
    if(pos >= size) {
      next();
      if(size <= 0) return 0;
    }
    return buffer[pos++];
  }
  
  /**
   * Reads the next buffer entry.
   */
  protected void next() {
    try {
      pos = 0;
      len += size;
      size = in.read(buffer);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the next character.
   * @return next character
   */
  public int readChar() {
    // support encodings..
    byte ch = readByte();
    if(encoding == Token.UTF16LE) readByte();
    if(encoding == Token.UTF16BE) ch = readByte();
    return ch;
  }

  /**
   * Closes the input stream.
   * @throws IOException IO Exception
   */
  public void close() throws IOException {
    if(in != null) in.close();
  }

  /**
   * Number of read bytes.
   * @return read bytes
   */
  public final int size() {
    return len + pos;
  }

  /**
   * Length of input.
   * @return read bytes
   */
  public final long length() {
    return length;
  }

  /**
   * Set input length.
   * @param l input length
   */
  public final void length(final long l) {
    length = l;
  }
}
