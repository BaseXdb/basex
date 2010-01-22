package org.basex.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class serves as a buffered wrapper for output streams.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class BufferedOutput extends OutputStream {
  /** Buffer size. */
  private final int bufsize;
  /** Byte buffer. */
  private final byte[] buffer;
  /** Reference to the data output stream. */
  private final OutputStream os;
  /** Current buffer position. */
  private int pos;

  /**
   * Constructor with a default buffer size.
   * @param outstream the OutputStream we operate on
   */
  public BufferedOutput(final OutputStream outstream) {
    this(outstream, IO.BLOCKSIZE);
  }

  /**
   * Constructor with a specific buffer size.
   * @param out the OutputStream we operate on
   * @param bufs buffer size
   */
  public BufferedOutput(final OutputStream out, final int bufs) {
    os = out;
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
