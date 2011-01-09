package org.basex.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps a {@link BufferInput} reference to a standard input stream.
 * {@code -1} is returned if the end of the stream is reached.
 * The method {@link #curr()} can be called to return the current stream
 * value, which is the first value to be returned, or the most recent value
 * that has been returned by {@link #read()}.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class WrapInputStream extends InputStream {
  /** Buffer input. */
  private final BufferInput bi;
  /** Current value. */
  private int curr;

  /**
   * Constructor.
   * @param buffer buffer input to be wrapped
   * @throws IOException I/O exception
   */
  public WrapInputStream(final BufferInput buffer) throws IOException {
    bi = buffer;
    read();
  }

  /**
   * Returns the current value.
   * @return current value
   */
  public int curr() {
    return curr;
  }

  @Override
  public int read() throws IOException {
    final int v = curr;
    if(v == -1) return -1;
    curr = bi.read();
    if(curr == 0) curr = -1;
    return v;
  }
}
