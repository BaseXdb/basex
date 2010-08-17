package org.basex.io;

import static org.basex.core.Text.*;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.basex.util.Token;

/**
 * This class caches the output bytes in an array, similar to the
 * {@link ByteArrayOutputStream} class. Bytes that exceed a optional maximum
 * are ignored.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CachedOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buf = new byte[8];

  /**
   * Default constructor.
   */
  public CachedOutput() { }

  /**
   * Constructor, specifying the maximum number of bytes to write.
   * Note that the limit might break unicode characters.
   * @param m maximum
   */
  public CachedOutput(final int m) {
    max = m;
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
    final byte[] chop = Token.token(DOTS);
    if(finished() && size >= chop.length) {
      // add dots at the end of the output
      System.arraycopy(chop, 0, buf, size - chop.length, chop.length);
    }
    return buf;
  }

  @Override
  public String toString() {
    return Token.string(toArray());
  }
}
