package main.util;

import java.util.Arrays;

/**
 * This class provides convenience methods for handling arrays
 * and serves as an extension to the {@link Arrays} class of Java.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Array {
  /** Private constructor. */
  private Array() { }

  /**
   * Resizes an array and adds an entry at the end.
   * @param ar array to be resized
   * @param e entry to be added
   * @param <T> array type
   * @return array
   */
  public static <T> T[] add(final T[] ar, final T e) {
    final int s = ar.length;
    final T[] t = Arrays.copyOf(ar, s + 1);
    t[s] = e;
    return t;
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
    final int s = ar.length - 1;
    move(ar, p + 1, -1, s - p);
    return Arrays.copyOf(ar, s);
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
