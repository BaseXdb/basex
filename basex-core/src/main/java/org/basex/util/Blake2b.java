package org.basex.util;

import java.nio.*;
import java.util.*;

/**
 * BLAKE2b algorithm by Jean-Philippe Aumasson, Samuel Neves, Zooko Wilcox-O’Hearn and
 * Christian Winnerlein (RFC 7693).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Blake2b {
  /** Initialization vector. */
  private static final long[] IV = { 0x6A09E667F3BCC908L, 0xBB67AE8584CAA73BL,
      0x3C6EF372FE94F82BL, 0xA54FF53A5F1D36F1L, 0x510E527FADE682D1L, 0x9B05688C2B3E6C1FL,
      0x1F83D9ABFB41BD6BL, 0x5BE0CD19137E2179L };
  /** Message schedule. */
  private static final byte[][] SIGMA = {
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
    { 14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 },
    { 11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4 },
    { 7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8 },
    { 9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13 },
    { 2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9 },
    { 12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11 },
    { 13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10 },
    { 6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5 },
    { 10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0 }
  };
  /** Block size. */
  private static final int BLOCK_SIZE = 128;

  /** Chained state. */
  private final long[] h = IV.clone();
  /** Current block. */
  private final byte[] block = new byte[BLOCK_SIZE];
  /** Little-endian view on the current block. */
  private final LongBuffer words = ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN).
      asLongBuffer();
  /** Working vector. */
  private final long[] v = new long[16];
  /** Message words. */
  private final long[] m = new long[16];
  /** Length of the digest in bytes. */
  private final int length;
  /** Number of bytes in the current block. */
  private int pos;
  /** Number of compressed bytes. */
  private long count;

  /**
   * Constructor.
   * @param length length of the digest in bytes (1-64)
   */
  public Blake2b(final int length) {
    if(length < 1 || length > 64) throw new IllegalArgumentException("Invalid length: " + length);
    this.length = length;
    h[0] ^= 0x01010000L ^ length;
  }

  /**
   * Computes a 512-bit digest.
   * @param input input
   * @return digest
   */
  public static byte[] digest(final byte[] input) {
    return new Blake2b(64).update(input).digest();
  }

  /**
   * Adds bytes to the digest.
   * @param input input
   * @return self reference
   */
  public Blake2b update(final byte[] input) {
    final int il = input.length;
    for(int i = 0; i < il;) {
      if(pos == BLOCK_SIZE) {
        count += BLOCK_SIZE;
        compress(false);
        pos = 0;
      }
      final int n = Math.min(BLOCK_SIZE - pos, il - i);
      System.arraycopy(input, i, block, pos, n);
      pos += n;
      i += n;
    }
    return this;
  }

  /**
   * Adds a 32-bit integer in little-endian order to the digest.
   * @param value value
   * @return self reference
   */
  public Blake2b update(final int value) {
    return update(new byte[] { (byte) value, (byte) (value >>> 8), (byte) (value >>> 16),
      (byte) (value >>> 24) });
  }

  /**
   * Finishes and returns the digest.
   * @return digest
   */
  public byte[] digest() {
    count += pos;
    Arrays.fill(block, pos, BLOCK_SIZE, (byte) 0);
    compress(true);
    final byte[] digest = new byte[length];
    for(int i = 0; i < length; i++) digest[i] = (byte) (h[i >>> 3] >>> ((i & 7) << 3));
    return digest;
  }

  /**
   * Compresses the current block.
   * @param last last block
   */
  private void compress(final boolean last) {
    words.get(0, m);
    System.arraycopy(h, 0, v, 0, 8);
    System.arraycopy(IV, 0, v, 8, 8);
    v[12] ^= count;
    if(last) v[14] = ~v[14];
    for(int r = 0; r < 12; r++) {
      final byte[] s = SIGMA[r % 10];
      mix(0, 4, 8, 12, m[s[0]], m[s[1]]);
      mix(1, 5, 9, 13, m[s[2]], m[s[3]]);
      mix(2, 6, 10, 14, m[s[4]], m[s[5]]);
      mix(3, 7, 11, 15, m[s[6]], m[s[7]]);
      mix(0, 5, 10, 15, m[s[8]], m[s[9]]);
      mix(1, 6, 11, 12, m[s[10]], m[s[11]]);
      mix(2, 7, 8, 13, m[s[12]], m[s[13]]);
      mix(3, 4, 9, 14, m[s[14]], m[s[15]]);
    }
    for(int i = 0; i < 8; i++) h[i] ^= v[i] ^ v[i + 8];
  }

  /**
   * Mixes four words of the working vector with two message words.
   * @param a first index
   * @param b second index
   * @param c third index
   * @param d fourth index
   * @param x first message word
   * @param y second message word
   */
  private void mix(final int a, final int b, final int c, final int d, final long x,
      final long y) {
    v[a] += v[b] + x;
    v[d] = Long.rotateRight(v[d] ^ v[a], 32);
    v[c] += v[d];
    v[b] = Long.rotateRight(v[b] ^ v[c], 24);
    v[a] += v[b] + y;
    v[d] = Long.rotateRight(v[d] ^ v[a], 16);
    v[c] += v[d];
    v[b] = Long.rotateRight(v[b] ^ v[c], 63);
  }
}
