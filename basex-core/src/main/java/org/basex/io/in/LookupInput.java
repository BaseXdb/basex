package org.basex.io.in;

import java.io.*;

/**
 * This input stream allows a lookup of the next byte to be returned
 * by an {@link InputStream} reference.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LookupInput extends InputStream {
  /** Input stream. */
  private final InputStream input;
  /** Current value. */
  private int curr;

  /**
   * Constructor.
   * @param input buffer input to be wrapped
   * @throws IOException I/O exception
   */
  public LookupInput(final InputStream input) throws IOException {
    this.input = input;
    read();
  }

  /**
   * Returns the current (next) value.
   * @return current value
   */
  public int lookup() {
    return curr;
  }

  @Override
  public int read() throws IOException {
    final int v = curr;
    if(v != -1) curr = input.read();
    return v;
  }

  @Override
  public void close() throws IOException {
    input.close();
  }
}
