package org.basex.io.in;

import org.basex.util.*;

/**
 * This class allows reading from a cached byte array.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ArrayInput extends BufferInput {
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
    return bpos < bsize ? buffer[bpos++] & 0xFF : -1;
  }
}
