package org.basex.io.in;

import java.io.*;

import org.basex.core.jobs.*;

/**
 * Input stream wrapper whose blocking reads are aborted if the surrounding job is stopped.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoppableInputStream extends FilterInputStream {
  /**
   * Constructor.
   * @param in input stream to wrap
   */
  public StoppableInputStream(final InputStream in) {
    super(in);
  }

  @Override
  public int read() throws IOException {
    try {
      return Job.run(in::read);
    } catch(final InterruptedException ex) {
      throw interrupted(ex);
    }
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    try {
      return Job.run(() -> in.read(b, off, len));
    } catch(final InterruptedException ex) {
      throw interrupted(ex);
    }
  }

  /**
   * Converts an interrupted exception to an I/O exception.
   * @param ex interrupted exception
   * @return I/O exception
   */
  private static InterruptedIOException interrupted(final InterruptedException ex) {
    final InterruptedIOException io = new InterruptedIOException();
    io.initCause(ex);
    return io;
  }
}
