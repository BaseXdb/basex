package org.basex.io.out;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class caches the output bytes in an array, similar to the
 * {@link ByteArrayOutputStream} class. Bytes that exceed an
 * optional maximum are ignored.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrayOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buffer = new byte[8];

  @Override
  public void write(final int value) {
    final int s = (int) size;
    if(s == max) return;
    if(s == buffer.length) buffer = Arrays.copyOf(buffer, Array.newSize(s));
    buffer[s] = (byte) value;
    size = s + 1;
  }

  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] toArray() {
    return Arrays.copyOf(buffer, (int) size);
  }

  /**
   * Returns the output as byte array, and invalidates the internal array.
   * Warning: the function must only be called if the output stream is discarded afterwards.
   * @return token
   */
  public byte[] finish() {
    final byte[] bffr = buffer;
    buffer = null;
    final int s = (int) size;
    return s == 0 ? EMPTY : s == bffr.length ? bffr : Arrays.copyOf(bffr, s);
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
    return buffer;
  }

  /**
   * Resets the internal buffer.
   */
  public void reset() {
    size = 0;
  }

  @Override
  public String toString() {
    return string(buffer, 0, (int) size);
  }
}
