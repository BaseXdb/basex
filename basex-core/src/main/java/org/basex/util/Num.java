package org.basex.util;

import java.util.*;

/**
 * <p>This class provides operations to compress and decompress 4-byte integer
 * values in byte arrays in order to save memory.</p>
 *
 * <p>The first two bits of a {@code Num} array indicate the range of the
 * compressed number:</p>
 *
 * <ul>
 * <li>{@code 00}: the number (0x00-0x3F) is encoded in the remaining 6 bits of
 * the current byte</li>
 * <li>{@code 01}: the number (0x40-0x3FFF) is encoded in 14 bits of the current
 * and the following byte</li>
 * <li>{@code 10}: the number (0x4000-0x3FFFFFFF) is encoded in 30 bits of the
 * current and the following three bytes</li>
 * <li>{@code 11}: the number (0x40000000-0xFFFFFFFF) is encoded in 32 bits of
 * the following four bytes</li>
 * </ul>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Num {
  /** Private constructor, preventing instantiation. */
  private Num() { }

  // STATIC METHODS ===========================================================

  /**
   * Creates a new number array, in which the first four bytes contain
   * the number of occupied bytes.
   * @param value initial value to be compressed and stored
   * @return new number array
   */
  public static byte[] newNum(final int value) {
    final int len = length(value);
    final byte[] array = new byte[4 + len];
    set(array, value, 4, len);
    size(array, 4 + len);
    return array;
  }

  /**
   * Creates a compressed representation of the specified value.
   * @param value value to be compressed
   * @return new number array
   */
  public static byte[] num(final int value) {
    final int len = length(value);
    final byte[] array = new byte[len];
    set(array, value, 0, len);
    return array;
  }

  /**
   * Compresses and adds a value to the specified array and
   * returns the resulting array.
   * @param array input array
   * @param value value to be added
   * @return resulting array (may be the same as input array)
   */
  public static byte[] add(final byte[] array, final int value) {
    final int len = length(value);
    final int pos = size(array);
    final byte[] tmp = check(array, pos, len);
    set(tmp, value, pos, len);
    size(tmp, pos + len);
    return tmp;
  }

  /**
   * Decompresses and returns a value from the specified byte array.
   * @param array array
   * @param pos position where the value is found
   * @return decompressed value
   */
  public static int get(final byte[] array, final int pos) {
    int p = pos;
    final int v = array[p++] & 0xFF;
    switch((v & 0xC0) >>> 6) {
      case 0: return v;
      case 1: return (v & 0x3F) << 8 | array[p] & 0xFF;
      case 2: return (v & 0x3F) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | array[p] & 0xFF;
      default: return (array[p++] & 0xFF) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | array[p] & 0xFF;
    }
  }

  /**
   * Compresses and stores an integer value to the specified byte array.
   * @param array array
   * @param value value to be stored
   * @param pos position where the value is to be stored
   */
  public static void set(final byte[] array, final int value, final int pos) {
    set(array, value, pos, length(value));
  }

  /**
   * Returns the length value of the specified array, stored in the first
   * four bytes.
   * @param array input array
   * @return array length
   */
  public static int size(final byte[] array) {
    return ((array[0] & 0xFF) << 24) + ((array[1] & 0xFF) << 16) +
      ((array[2] & 0xFF) << 8) + (array[3] & 0xFF);
  }

  /**
   * Stores the specified length value in the first bytes of the
   * specified array.
   * @param array input array
   * @param length length to be stored
   */
  public static void size(final byte[] array, final int length) {
    array[0] = (byte) (length >>> 24);
    array[1] = (byte) (length >>> 16);
    array[2] = (byte) (length >>> 8);
    array[3] = (byte) length;
  }

  /**
   * Returns the compressed length of the value at the specified position.
   * @param array array
   * @param pos position where the value is found
   * @return value length
   */
  public static int length(final byte[] array, final int pos) {
    final int v = (array[pos] & 0xFF) >>> 6;
    return v == 0 ? 1 : v == 1 ? 2 : v == 2 ? 4 : 5;
  }

  /**
   * Returns the compressed length of the specified value.
   * @param v integer value
   * @return value length
   */
  public static int length(final int v) {
    return v < 0 || v > 0x3FFFFFFF ? 5 : v > 0x3FFF ? 4 : v > 0x3F ? 2 : 1;
  }

  // PRIVATE STATIC METHODS ===================================================

  /**
   * Resizes the specified array if no space is left.
   * @param a array to be resized
   * @param p current array position
   * @param l length of new entry
   * @return new array
   */
  private static byte[] check(final byte[] a, final int p, final int l) {
    final int s = a.length;
    return p + l < s ? a : Arrays.copyOf(a, s + Math.max(l, s >> 3));
  }

  /**
   * Compresses and writes an integer value to the specified byte array.
   * @param a array
   * @param v value to be written
   * @param p position
   * @param l value length
   */
  private static void set(final byte[] a, final int v, final int p,
      final int l) {

    int i = p;
    if(l == 5) {
      a[i++] = (byte) 0xC0;
      a[i++] = (byte) (v >>> 24);
      a[i++] = (byte) (v >>> 16);
      a[i++] = (byte) (v >>> 8);
    } else if(l == 4) {
      a[i++] = (byte) (v >>> 24 | 0x80);
      a[i++] = (byte) (v >>> 16);
      a[i++] = (byte) (v >>> 8);
    } else if(l == 2) {
      a[i++] = (byte) (v >>> 8 | 0x40);
    }
    a[i] = (byte) v;
  }
}
