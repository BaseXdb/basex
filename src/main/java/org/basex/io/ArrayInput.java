package org.basex.io;

/**
 * This class allows reading from a cached byte array.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
