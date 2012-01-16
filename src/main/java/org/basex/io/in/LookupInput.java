package org.basex.io.in;

import java.io.IOException;
import java.io.InputStream;

/**
 * This input stream allows a lookup of the next byte to be returned
 * by an {@link InputStream} reference.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class LookupInput extends InputStream {
  /** Input stream. */
  private final InputStream input;
  /** Current value. */
  private int curr;

  /**
   * Constructor.
   * @param in buffer input to be wrapped
   * @throws IOException I/O exception
   */
  public LookupInput(final InputStream in) throws IOException {
    input = in;
    read();
  }

  /**
   * Returns the current value.
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
}
