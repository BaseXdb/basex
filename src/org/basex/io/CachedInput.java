package org.basex.io;

/**
 * This class allows reading from a cached byte array.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CachedInput extends BufferInput {
  /**
   * Initializes the file reader.
   * @param input input bytes
   */
  public CachedInput(final byte[] input) {
    super(input);
  }
  
  /**
   * Returns the next byte.
   * @return next byte
   */
  @Override
  public byte readByte() {
    return pos < buffer.length ? buffer[pos++] : 0;
  }
}
