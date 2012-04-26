package org.basex.io.out;

import java.io.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class caches the output bytes in an array, similar to the
 * {@link ByteArrayOutputStream} class. Bytes that exceed an
 * optional maximum are ignored.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ArrayOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buf = new byte[8];

  /**
   * Sets the maximum number of bytes to be written.
   * Note that the limit might break unicode characters.
   * @param m maximum
   * @return self reference
   */
  public ArrayOutput max(final int m) {
    max = m;
    return this;
  }

  @Override
  public void write(final int b) {
    final int s = (int) size;
    if(s == max) return;
    if(s == buf.length) buf = Arrays.copyOf(buf, s << 1);
    buf[s] = (byte) b;
    size = s + 1;
  }

  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] toArray() {
    return Arrays.copyOf(buf, (int) size);
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
    return buf;
  }

  /**
   * Resets the internal buffer.
   */
  public void reset() {
    size = 0;
  }

  @Override
  public String toString() {
    return Token.string(buf, 0, (int) size);
  }
}
