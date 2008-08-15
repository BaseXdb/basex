package org.basex.util;

/**
 * This class provides operations to compress and decompress integer values
 * in byte arrays to save memory.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Num {
  /** Private constructor, preventing instantiation. */
  private Num() { }
  
  // STATIC METHODS ===========================================================
  
  /**
   * Returns an empty number array.
   * @return new array
  public static byte[] newNum() {
    return new byte[] { 0, 0, 0, 4 };
  }
   */

  /**
   * Creates a new number array.
   * @param val initial value to be stored
   * @return new number array
   */
  public static byte[] newNum(final int val) {
    final int len = len(val);
    final byte[] array = new byte[4 + len];
    add(array, val, 4, len);
    size(array, 4 + len);
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
    byte[] tmp = check(array, pos, len);
    add(tmp, val, pos, len);
    size(tmp, pos + len);
    return tmp;
  }

  /**
   * Creates and returns a compressed array from the specified integer array.
   * The first value in the specified array denotes the number of entries.
   * @param vals values to be written
   * @return new array
   */
  public static byte[] create(final int[] vals) {
    byte[] tmp = new byte[vals.length << 1];
    int vs = vals[0];
    int pos = 4;
    for(int i = 1; i <= vs; i++) {
      final int len = len(vals[i]);
      tmp = check(tmp, pos, len);
      add(tmp, vals[i], pos, len);
      pos += len;
    }
    size(tmp, pos);
    return pos == tmp.length ? tmp : Array.finish(tmp, pos);
  }
  
  /**
   * Resizes the specified array by 12,5%.
   * @param tmp array to be resized
   * @param pos current array position
   * @param len length of new entry
   * @return new array
   */
  private static byte[] check(final byte[] tmp, final int pos, final int len) {
    final int s = tmp.length;
    if(pos + len < s) return tmp;
    final byte[] t = new byte[s + Math.max(len, s >> 3)];
    System.arraycopy(tmp, 0, t, 0, s);
    return t;
  }

  /**
   * Compresses and writes an integer value to the specified byte array.
   * @param array array
   * @param v value to be written
   * @param p position
   * @param l value length
   */
  private static void add(final byte[] array, final int v, final int p,
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
      case 1: return (v & 0x3F) << 8 | (array[p++] & 0xFF);
      case 2: return (v & 0x3F) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | (array[p++] & 0xFF);
      default: return (array[p++] & 0xFF) << 24 | (array[p++] & 0xFF) << 16 |
        (array[p++] & 0xFF) << 8 | (array[p++] & 0xFF);
    }
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
   * Writes the specified length in the first bytes of the specified array.
   * @param array array
   * @param len new length
   */
  private static void size(final byte[] array, final int len) {
    array[0] = (byte) (len >>> 24);
    array[1] = (byte) (len >>> 16);
    array[2] = (byte) (len >>> 8);
    array[3] = (byte) len;
  }
  
  /**
   * Returns the compressed length of the specified value.
   * @param v integer value
   * @return value length
   */
  private static int len(final int v) {
    return v < 0 || v > 0x3FFFFFFF ? 5 : v > 0x3FFF ? 4 : v > 0x3F ? 2 : 1;
  }

  /**
   * Finishes compressed id array.
   * @param array to be finished
   * @return byte array
  public static byte[] finish(final byte[] array) {
    final int s = size(array);
    return s == array.length ? array : Array.finish(array, s);
  }
   */
}
