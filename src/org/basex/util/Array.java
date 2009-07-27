package org.basex.util;

/**
 * This class provides convenience methods for handling arrays.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Array {
  /** Empty integer array. */
  public static final int[] NOINTS = {};
  /** Empty integer array. */
  public static final int[][] NOINTS2 = {};

  /** Private constructor. */
  private Array() { }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static boolean[] extend(final boolean[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static int[] extend(final int[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static int[][] extend(final int[][] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static long[] extend(final long[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static byte[] extend(final byte[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static byte[][] extend(final byte[][] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @return finished array
   */
  public static String[] extend(final String[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size.
   * @param ar array to be resized
   * @param <T> array type
   * @return array
   */
  public static <T> T[] extend(final T[] ar) {
    final int s = ar.length;
    return resize(ar, s, s << 1);
  }

  /**
   * Doubles the array size if necessary.
   * @param ar array to be resized
   * @param s array size
   * @param <T> array type
   * @return array
   */
  public static <T> T[] check(final T[] ar, final int s) {
    return s == ar.length ? resize(ar, s, s << 1) : ar;
  }

  /**
   * Resizes an array and adds an entry at the end.
   * @param ar array to be resized
   * @param e entry to be added
   * @return finished array
   */
  public static byte[][] add(final byte[][] ar, final byte[] e) {
    final int s = ar.length;
    final byte[][] b = resize(ar, s, s + 1);
    b[s] = e;
    return b;
  }

  /**
   * Resizes an array and adds an entry at the end.
   * @param ar array to be resized
   * @param e entry to be added
   * @param s0 start position in e
   * @param s1 end position in e
   * @return finished array
   */
  public static byte[] add(final byte[] ar, final byte[] e, final int s0,
      final int s1) {
    final int sar = ar.length;
    final int se = s1 - s0;
    final byte[] b = resize(ar, sar, sar + se);
    System.arraycopy(e, s0, b, sar, se);
    return b;
  }

  /**
   * Resizes an array and adds an entry at the end.
   * @param ar array to be resized
   * @param e entry to be added
   * @return finished array
   */
  public static int[] add(final int[] ar, final int e) {
    final int s = ar.length;
    final int[] b = resize(ar, s, s + 1);
    b[s] = e;
    return b;
  }

  /**
   * Resizes an array and adds an entry at the end.
   * @param ar array to be resized
   * @param e entry to be added
   * @param <T> array type
   * @return array
   */
  public static <T> T[] add(final T[] ar, final T e) {
    final int s = ar.length;
    final T[] t = resize(ar, s, s + 1);
    t[s] = e;
    return t;
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static int[] finish(final int[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static double[] finish(final double[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static String[] finish(final String[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static boolean[] finish(final boolean[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static byte[] finish(final byte[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param size final size
   * @return finished array
   */
  public static byte[][] finish(final byte[][] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Optimizes the array size.
   * @param ar array to be resized
   * @param <T> array type
   * @param size final size
   * @return array
   */
  public static <T> T[] finish(final T[] ar, final int size) {
    final int s = ar.length;
    return resize(ar, s > size ? size : s, size);
  }

  /**
   * Convenience method for resizing a String array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static String[] resize(final String[] ar, final int os, final int ns) {
    final String[] tmp = new String[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing an integer array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static int[] resize(final int[] ar, final int os, final int ns) {
    final int[] tmp = new int[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing a double array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static double[] resize(final double[] ar, final int os, final int ns) {
    final double[] tmp = new double[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing an integer array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static int[][] resize(final int[][] ar, final int os, final int ns) {
    final int[][] tmp = new int[ns][];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing an integer array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static long[] resize(final long[] ar, final int os, final int ns) {
    final long[] tmp = new long[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing a boolean array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static boolean[] resize(final boolean[] ar, final int os,
      final int ns) {

    final boolean[] tmp = new boolean[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing a byte array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static byte[] resize(final byte[] ar, final int os, final int ns) {
    final byte[] tmp = new byte[ns];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing a two-dimensional byte array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @return resized array
   */
  public static byte[][] resize(final byte[][] ar, final int os, final int ns) {
    final byte[][] tmp = new byte[ns][];
    System.arraycopy(ar, 0, tmp, 0, os);
    return tmp;
  }

  /**
   * Convenience method for resizing a generic array.
   * @param ar array to be resized
   * @param os old size
   * @param ns new size
   * @param <T> array type
   * @return array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] resize(final T[] ar, final int os, final int ns) {
    final T[] copy = (T[]) java.lang.reflect.Array.newInstance(
        ar.getClass().getComponentType(), ns);
    System.arraycopy(ar, 0, copy, 0, os);
    return copy;
  }

  /**
   * Copies the source token into the target token.
   * @param src source array
   * @param trg target array
   * @param s start position
   */
  public static void copy(final byte[] src, final byte[] trg, final int s) {
    System.arraycopy(src, 0, trg, s, src.length);
  }

  /**
   * Copies the source array into the target array, starting from
   * the specified offset.
   * @param src source array
   * @param trg target array
   * @param s start position
   */
  public static void copy(final Object[] src, final Object[] trg, final int s) {
    System.arraycopy(src, 0, trg, s, src.length);
  }

  /**
   * Checks two int arrays for equality.
   * @param i1 first array
   * @param i2 second array
   * @return result of check
   */
  public static boolean eq(final int[] i1, final int[] i2) {
    final int il = i1.length;
    if(il != i2.length) return false;
    for(int i = 0; i < il; i++) if(i1[i] != i2[i]) return false;
    return true;
  }

  /**
   * Checks two byte arrays for equality.
   * @param i1 first array
   * @param i2 second array
   * @return result of check
   */
  public static boolean eq(final byte[] i1, final byte[] i2) {
    final int il = i1.length;
    if(il != i2.length) return false;
    for(int i = 0; i < il; i++) if(i1[i] != i2[i]) return false;
    return true;
  }

  /**
   * Creates a new array from the source array and with the specified length.
   * @param src source array
   * @param pos array position
   * @param len array length
   * @return new array
   */
  public static byte[] create(final byte[] src, final int pos, final int len) {
    final byte[] tmp = new byte[len];
    System.arraycopy(src, pos, tmp, 0, len);
    return tmp;
  }

  /**
   * Moves entries inside an array.
   * @param ar array
   * @param pos position
   * @param off move offset
   * @param l length
   */
  public static void move(final Object ar, final int pos, final int off,
      final int l) {
    System.arraycopy(ar, pos, ar, pos + off, l);
  }

  /**
   * Removes an array entry at the specified position.
   * @param ar array to be resized
   * @param p position
   * @param <T> array type
   * @return array
   */
  public static <T> T[] delete(final T[] ar, final int p) {
    move(ar, p + 1, -1, ar.length - p - 1);
    return finish(ar, ar.length - 1);
  }

  /**
   * Sorts the specified tokens and returns an integer array
   * with offsets to of the sorted tokens.
   * @param tok token array to sort by
   * @param num numeric sort
   * @param asc ascending
   * @return sorted integer array
   */
  public static int[] createOrder(final byte[][] tok, final boolean num,
      final boolean asc) {
    final IntList il = number(tok.length);
    il.sort(tok, num, asc);
    return il.finish();
  }

  /**
   * Sorts the specified numeric tokens and returns an integer array
   * with offsets to of the sorted tokens.
   * @param tok token array to sort by
   * @param asc ascending
   * @return sorted integer array
   */
  public static int[] createOrder(final double[] tok, final boolean asc) {
    final IntList il = number(tok.length);
    il.sort(tok, asc);
    return il.finish();
  }

  /**
   * Returns an integer list with a number list.
   * @param l array size
   * @return number list
   */
  private static IntList number(final int l) {
    final int[] tmp = new int[l];
    for(int i = 0; i < l; i++) tmp[i] = i;
    return new IntList(tmp);
  }
}
