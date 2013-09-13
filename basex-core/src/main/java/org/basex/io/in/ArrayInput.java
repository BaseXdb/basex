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
   * @param in input bytes
   */
  public ArrayInput(final byte[] in) {
    super(in);
  }

  /**
   * Constructor, specifying the string to be read.
   * @param in input bytes
   */
  public ArrayInput(final String in) {
    this(Token.token(in));
  }

  @Override
  protected int readByte() {
    return bpos < bsize ? buffer[bpos++] & 0xFF : -1;
  }
}
