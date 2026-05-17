package org.basex.io.out;

import java.io.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class caches the output bytes in an array, similar to {@link ByteArrayOutputStream}.
 * This implementation is faster because functions are not synchronized.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayOutput extends PrintOutput {
  /** Byte buffer. */
  private byte[] buffer = new byte[Array.INITIAL_CAPACITY];

  /**
   * Constructor.
   */
  public ArrayOutput() {
    super((OutputStream) null);
  }

  @Override
  public void write(final int b) {
    final int s = (int) size;
    if(s == max) return;

    byte[] bffr = buffer;
    if(s == bffr.length) bffr = Arrays.copyOf(bffr, Array.newCapacity(s));
    bffr[s] = (byte) b;
    buffer = bffr;
    size = s + 1;
  }

  @Override
  public void write(final byte[] b, final int off, final int len) {
    final int s = (int) size;
    final int free = (int) Math.min(len, max - s);
    if(free <= 0) return;

    byte[] bffr = buffer;
    final int needed = Array.checkCapacity((long) s + free);
    if(needed > bffr.length) {
      bffr = Arrays.copyOf(bffr, Math.max(Array.newCapacity(bffr.length), needed));
    }
    System.arraycopy(b, off, bffr, s, free);
    buffer = bffr;
    size = s + free;
  }

  @Override
  public void print(final byte[] token) {
    final int tl = token.length;
    if(tl == 0) return;
    long ll = lineLength;
    for(int i = 0; i < tl; i++) {
      final byte b = token[i];
      if(b == '\n') ll = 0;
      else if((b & 0xC0) != 0x80) ++ll;
    }
    lineLength = ll;
    write(token, 0, tl);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }

  /**
   * Returns the output as byte array.
   * @return byte array
   */
  public byte[] toArray() {
    return Arrays.copyOf(buffer, (int) size);
  }

  /**
   * Returns an array with all elements and resets the internal buffer.
   * @return array
   */
  public byte[] next() {
    final byte[] lst = toArray();
    reset();
    return lst;
  }

  /**
   * Returns the output as byte array, and invalidates the internal array.
   * Warning: the function must only be called if the output stream is discarded afterward.
   * @return token
   */
  public byte[] finish() {
    final byte[] bffr = buffer;
    buffer = null;
    final int s = (int) size;
    return s == 0 ? Token.EMPTY : s == bffr.length ? bffr : Arrays.copyOf(bffr, s);
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
    return Token.string(buffer, 0, (int) size);
  }
}
