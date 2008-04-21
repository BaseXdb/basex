package org.basex.util;

/**
 * This class provides operations to compress and decompress integer values
 * in byte arrays to save memory. The first four bytes of the array store the 
 * array length. {@link #more()} and {@link #next()} can be called to iterate
 * through the stored values.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Num {
  /** Word position flag. */
  private final boolean word;
  /** Num iterator. */
  private final byte[] iter;
  /** Num iterator position. */
  private int itpos;

  /**
   * Constructor for iterating a {@link Num} instance.
   * @param array array to be iterated
   * @param w flag stating if word positions are stored in the array
   */
  public Num(final byte[] array, final boolean w) {
    iter = array;
    itpos = 4;
    word = w;
  }

  /**
   * Checks if more numbers are to be iterated.
   * @return true if more numbers are found
   */
  public boolean more() {
    return itpos < iter.length;
  }
  
  /**
   * Returns next id.
   * @return next id
   */
  public int id() {
    return Num.read(iter, itpos);
  }
  
  /**
   * Returns next word position.
   * @return next word position
   */
  public int pos() {
    return Num.read(iter, itpos + Num.len(iter, itpos));
  }
  
  /**
   * Jumps to next id.
   */
  public void next() {
    itpos += Num.len(iter, itpos);
    if(word) itpos += Num.len(iter, itpos);
  }
  
  /**
   * Returns an empty number array.
   * @return new array
   */
  public static byte[] newNum() {
    return new byte[] { 0, 0, 0, 4 };
  }

  /**
   * Creates a new number array.
   * @param val initial value to be written
   * @return new array
   */
  public static byte[] newNum(final int val) {
    final int len = len(val);
    final byte[] array = new byte[4 + len];
    add(array, val, 4, len);
    size(array, 4 + len);
    return array;
  }

  /**
   * Compresses and writes an integer value to the specified byte array.
   * @param array array
   * @param val value to be written
   * @return new array
   */
  public static byte[] add(final byte[] array, final int val) {
    final int len = len(val);
    final int pos = size(array);
    byte[] a = array;
    final int s = array.length;
    if(pos + len >= s) {
      a = new byte[s + Math.max(len, s >> 3)];
      System.arraycopy(array, 0, a, 0, s);
    }
    add(a, val, pos, len);
    size(a, pos + len);
    return a;
  }

  /**
   * Creates a compressed array from the specified integer array.
   * The first value in the specified array denotes the number of entries.
   * @param vals values to be written
   * @return new array
   */
  public static byte[] create(final int[] vals) {
    byte[] tmp = new byte[vals.length << 2];
    int vs = vals[0];
    int pos = 4;
    for(int i = 1; i <= vs; i++) {
      final int len = len(vals[i]);
      final int s = tmp.length;
      if(pos + len >= s) {
        byte[] t = new byte[s + Math.max(len, s >> 3)];
        System.arraycopy(tmp, 0, t, 0, s);
        tmp = t;
      }
      add(tmp, vals[i], pos, len);
      pos += len;
    }
    size(tmp, pos);
    return pos == tmp.length ? tmp : Array.finish(tmp, pos);
  }

  /**
   * Adds a byte array to the specified array.
   * @param array array to be modified
   * @param v array to be added
   * @return modified array
   */
  public static byte[] add(final byte[] array, final byte[] v) {
    final int p = size(array);
    final int l = len(v.length);
    final int t = v.length;
    byte[] a = array;
    while(p + l + t >= a.length) a = Array.extend(a);
    add(a, t, p, l);
    Array.copy(v, a, p + l);
    size(a, p + l + t);
    return a;
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
   * Finishes compressed id array.
   * @param array to be finished
   * @return byte array
   */
  public static byte[] finish(final byte[] array) {
    final int s = size(array);
    return s == array.length ? array : Array.finish(array, s);
  }

  /**
   * Returns length of the specified array.
   * @param array array to be evaluated
   * @return array length
   */
  public static int size(final byte[] array) {
    return ((array[0] & 0xFF) << 24) + ((array[1] & 0xFF) << 16) +
      ((array[2] & 0xFF) << 8) + (array[3] & 0xFF);
  }
  
  /**
   * Set length in specified array.
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
   * Returns integer length.
   * @param v integer value
   * @return value length
   */
  private static int len(final int v) {
    return v < 0 || v > 0x3FFFFFFF ? 5 : v > 0x3FFF ? 4 : v > 0x3F ? 2 : 1;
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
}
