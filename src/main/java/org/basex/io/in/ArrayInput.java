package org.basex.io.in;

import org.basex.util.Token;

/**
 * This class allows reading from a cached byte array.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ArrayInput extends BufferInput {
  /** Marker. */
  private int mark;

  /**
   * Constructor, specifying the byte array to be read.
   * @param input input bytes
   */
  public ArrayInput(final byte[] input) {
    super(input);
  }

  /**
   * Constructor, specifying the string to be read.
   * @param input input bytes
   */
  public ArrayInput(final String input) {
    this(Token.token(input));
  }

  @Override
  public int read() {
    return pos < size ? buffer[pos++] & 0xFF : -1;
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public synchronized void mark(final int m) {
    mark = m;
  }

  @Override
  public synchronized void reset() {
    pos = mark;
  }
}
