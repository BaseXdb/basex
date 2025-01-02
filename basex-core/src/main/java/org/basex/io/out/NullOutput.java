package org.basex.io.out;

import java.io.*;

/**
 * This output stream swallows all data it receives.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NullOutput extends PrintOutput {
  /**
   * Constructor.
   */
  public NullOutput() {
    super((OutputStream) null);
  }

  @Override
  public void write(final int value) {
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }
}
