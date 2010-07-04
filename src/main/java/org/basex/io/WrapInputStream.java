package org.basex.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps a {@link BufferInput} reference to a standard input stream.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class WrapInputStream extends InputStream {
  /** Buffer input. */
  private final BufferInput bi;
  /** Flag for reading more bytes. */
  private boolean more = true;

  /**
   * Constructor.
   * @param buffer buffer input to be wrapped
   */
  public WrapInputStream(final BufferInput buffer) {
    bi = buffer;
  }

  @Override
  public int read() throws IOException {
    // always return -1 if end of stream was reached
    if(more) {
      final int c = bi.read();
      if(c != 0) return c;
      more = false;
    }
    return -1;
  }
}
