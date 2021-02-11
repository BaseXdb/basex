package org.basex.io.out;

import java.io.*;

/**
 * This output stream swallows all data it receives.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public void write(final int value) { }
}
