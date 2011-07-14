package org.basex.io;

import java.util.Arrays;
import org.basex.util.Token;

/**
 * This class caches the output bytes in an array, similar to the
 * {@link java.io.ByteArrayOutputStream} class. Bytes that exceed an
 * optional maximum are ignored.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ArrayOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buf = new byte[8];

  /**
   * Default constructor.
   */
  public ArrayOutput() { }

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
    if(size == max) return;
    if(size == buf.length) buf = Arrays.copyOf(buf, size << 1);
    buf[size++] = (byte) b;
  }

  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] toArray() {
    return Arrays.copyOf(buf, size);
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

  @Override
  public String toString() {
    return Token.string(buf, 0, size);
  }
}
