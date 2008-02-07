package org.basex.io;

import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class caches the output bytes in an array.
 * If a maximum is specified, the returned byte array contains a notice
 * that the output was chopped in case there was more data than specified
 * by the limit.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CachedOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buf = new byte[8];
  /** Maximum numbers of bytes to write. */
  private final int max;

  /**
   * Default constructor.
   */
  public CachedOutput() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Constructor, specifying the maximum number of bytes to write.
   * @param m maximum
   */
  public CachedOutput(final int m) {
    max = m;
  }
  
  @Override
  public void write(final int b) {
    if(size == max) return;
    if(size == buf.length) buf = Array.extend(buf);
    buf[size++] = (byte) b;
  }

  /**
   * Resets the output array.
   */
  public void reset() {
    size = 0;
  }
  
  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] finish() {
    if(finished()) {
      final byte[] chop = IOConstants.RESULTCHOP;
      Array.copy(chop, buf, max - chop.length);
    }
    return Array.finish(buf, size);
  }

  @Override
  public boolean finished() {
    return size == max;
  }
  
  @Override
  public String toString() {
    return Token.string(finish());
  }
}
