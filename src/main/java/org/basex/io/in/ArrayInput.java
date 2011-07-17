package org.basex.io.in;

/**
 * This class allows reading from a cached byte array.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ArrayInput extends BufferInput {
  /**
   * Initializes the file reader.
   * @param input input bytes
   */
  public ArrayInput(final byte[] input) {
    super(input);
  }

  /**
   * Returns the next byte.
   * @return next byte
   */
  @Override
  public byte readByte() {
    return pos < length ? buffer[pos++] : 0;
  }
}
