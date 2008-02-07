package org.basex.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class serves as a buffered wrapper for input streams.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BufferInput {
  /** Byte buffer. */
  protected final byte[] buffer;
  /** Current buffer position. */
  protected int pos;
  /** Current buffer size. */
  private int size;
  /** Number of read bytes. */
  private int len;
  /** Reference to the data input stream. */
  private InputStream in;
  /** File length. */
  private long length;

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
    this(file, new byte[4096]);
  }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @param buf input buffer
   * @throws IOException IO Exception
   */
  public BufferInput(final String file, final byte[] buf) throws IOException {
    this(new File(file), buf);
  }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @param buf input buffer
   * @throws IOException IO Exception
   */
  public BufferInput(final File file, final byte[] buf) throws IOException {
    this(buf);
    in = new FileInputStream(file);
    length = file.length();
  }

  /**
   * Empty constructor.
   * @param buf buffer
   */
  public BufferInput(final byte[] buf) {
    buffer = buf;
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
      try {
        pos = 0;
        len += size;
        size = in.read(buffer);
        if(size <= 0) return 0;
      } catch(final IOException ex) {
        ex.printStackTrace();
      }
    }
    return buffer[pos++];
  }

  /**
   * Closes the input stream.
   * @throws IOException IO Exception
   */
  public final void close() throws IOException {
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
}
