package org.basex.io.out;

import java.io.*;

import org.basex.io.*;

/**
 * This class uses a byte buffer to speed up output stream processing.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class BufferOutput extends OutputStream {
  /** Buffer size. */
  private final int bufsize;
  /** Byte buffer. */
  private final byte[] buffer;
  /** Reference to the data output stream. */
  private final OutputStream os;
  /** Current buffer position. */
  private int pos;

  /**
   * Constructor.
   * @param file target file
   * @throws IOException I/O exception
   */
  public BufferOutput(final String file) throws IOException {
    this(new FileOutputStream(file), IO.BLOCKSIZE);
  }

  /**
   * Constructor with a default buffer size.
   * @param os the stream to write to
   */
  public BufferOutput(final OutputStream os) {
    this(os, IO.BLOCKSIZE);
  }

  /**
   * Constructor with a specific buffer size.
   * @param os the stream to write to
   * @param bufs buffer size
   */
  public BufferOutput(final OutputStream os, final int bufs) {
    this.os = os;
    buffer = new byte[bufs];
    bufsize = bufs;
  }

  @Override
  public void write(final int b) throws IOException {
    if(pos == bufsize) flush();
    buffer[pos++] = (byte) b;
  }

  @Override
  public void flush() throws IOException {
    os.write(buffer, 0, pos);
    pos = 0;
  }

  @Override
  public void close() throws IOException {
    flush();
    os.close();
  }
}
