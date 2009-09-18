package org.basex.io;

import static org.basex.core.Text.*;

import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class caches the output bytes in an array.
 * If a maximum is specified, the returned byte array contains a notice
 * that the output was chopped in case there was more data than specified
 * by the limit.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CachedOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buf = new byte[8];

  /**
   * Default constructor.
   */
  public CachedOutput() {
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
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] finish() {
    return Array.finish(buf, size);
  }

  @Override
  public boolean finished() {
    return size == max;
  }

  /**
   * Returns the internal buffer.
   * @return buffer
   */
  public byte[] buffer() {
    if(finished()) {
      final byte[] chop = Token.token(DOTS);
      Array.copy(chop, buf, size - chop.length);
    }
    return buf;
  }

  @Override
  public String toString() {
    return Token.string(finish());
  }
}
