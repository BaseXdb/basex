package org.basex.util;

import java.util.Arrays;

/**
 * This class provides operations to compress and decompress 4-byte integer
 * values in byte arrays to save memory.<br/>
 *
 * The first two bits of a {@code Num} array indicate the range of the
 * compressed number:
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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Num {
  /** Private constructor, preventing instantiation. */
  private Num() { }

  // STATIC METHODS ===========================================================

  /**
   * Creates a new number array, in which the first four bytes contain
   * the array size.
   * @param val initial value to be stored
   * @return new number array
   */
  public static byte[] newNum(final int val) {
    final int len = len(val);
    final byte[] array = new byte[4 + len];
    write(array, val, 4, len);
    size(array, 4 + len);
    return array;
  }

  /**
   * Creates an array with the specified number.
   * @param val initial value to be stored
   * @return new number array
   */
  public static byte[] num(final int val) {
    final int len = len(val);
    final byte[] array = new byte[len];
    write(array, val, 0, len);
    return array;
  }

  /**
   * Compresses and writes an integer value to the specified array and
   * returns the array.
   * @param array array
   * @param val value to be written
   * @return new array
   */
  public static byte[] add(final byte[] array, final int val) {
    final int len = len(val);
    final int pos = size(array);
    final byte[] tmp = check(array, pos, len);
    write(tmp, val, pos, len);
    size(tmp, pos + len);
    return tmp;
  }

  /**
   * Reads and decompresses an integer value from the specified byte array.
   * @param array array
   * @param pos position to parse
   * @return decompressed value
   */
  public static int read(final byte[] array, final int pos) {
    int p = pos;
    final int v = array[p++] & 0xFF;
    switch((v & 0xC0) >>> 6) {
      case 0: return v;
      case 1: return (v & 0x3F) << 8 | array[p++] & 0xFF;
      case 2: return (v & 0x3F) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | array[p++] & 0xFF;
      default: return (array[p++] & 0xFF) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | array[p++] & 0xFF;
    }
  }

  /**
   * Compresses and writes an integer value to the specified byte array.
   * @param array array
   * @param v value to be written
   * @param p position
   */
  public static void write(final byte[] array, final int v, final int p) {
    write(array, v, p, len(v));
  }

  /**
   * Returns the length of the specified array, stored in the first four bytes.
   * @param array array to be evaluated
   * @return array length
   */
  public static int size(final byte[] array) {
    return ((array[0] & 0xFF) << 24) + ((array[1] & 0xFF) << 16) +
      ((array[2] & 0xFF) << 8) + (array[3] & 0xFF);
  }

  /**
   * Writes the specified length in the first bytes of the specified array.
   * @param array array
   * @param len length to be written
   */
  public static void size(final byte[] array, final int len) {
    array[0] = (byte) (len >>> 24);
    array[1] = (byte) (len >>> 16);
    array[2] = (byte) (len >>> 8);
    array[3] = (byte) len;
  }

  /**
   * Returns integer length.
   * @param array array
   * @param val integer value
   * @return value length
   */
  public static int len(final byte[] array, final int val) {
    final int v = (array[val] & 0xFF) >>> 6;
    return v == 0 ? 1 : v == 1 ? 2 : v == 2 ? 4 : 5;
  }

  /**
   * Returns the compressed length of the specified value.
   * @param v integer value
   * @return value length
   */
  public static int len(final int v) {
    return v < 0 || v > 0x3FFFFFFF ? 5 : v > 0x3FFF ? 4 : v > 0x3F ? 2 : 1;
  }

  // PRIVATE STATIC METHODS ===================================================

  /**
   * Resizes the specified array by 12,5%.
   * @param tmp array to be resized
   * @param pos current array position
   * @param len length of new entry
   * @return new array
   */
  private static byte[] check(final byte[] tmp, final int pos, final int len) {
    final int s = tmp.length;
    return pos + len < s ? tmp : Arrays.copyOf(tmp, s + Math.max(len, s >> 3));
  }

  /**
   * Compresses and writes an integer value to the specified byte array.
   * @param array array
   * @param v value to be written
   * @param p position
   * @param l value length
   */
  private static void write(final byte[] array, final int v, final int p,
      final int l) {
    int i = p;
    if(l == 5) {
      array[i++] = (byte) 0xC0;
      array[i++] = (byte) (v >>> 24);
      array[i++] = (byte) (v >>> 16);
      array[i++] = (byte) (v >>> 8);
    } else if(l == 4) {
      array[i++] = (byte) (v >>> 24 | 0x80);
      array[i++] = (byte) (v >>> 16);
      array[i++] = (byte) (v >>> 8);
    } else if(l == 2) {
      array[i++] = (byte) (v >>> 8 | 0x40);
    }
    array[i++] = (byte) v;
  }
}
