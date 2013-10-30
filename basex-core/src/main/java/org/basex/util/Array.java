package org.basex.util;

import java.util.*;

import org.basex.util.list.*;

/**
 * This class provides convenience methods for handling arrays
 * and serves as an extension to the {@link Arrays} class of Java.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Array {
  /** Initial default size for new arrays. */
  public static final int CAPACITY = 1 << 3;
  /** Default factor for resizing dynamic arrays. */
  public static final double RESIZE = 1.5;
  /** Private constructor. */
  private Array() { }

  /**
   * Copies the specified array.
   * @param a array to be copied
   * @param s new array size
   * @return new array
   */
  public static byte[][] copyOf(final byte[][] a, final int s) {
    final byte[][] tmp = new byte[s][];
    System.arraycopy(a, 0, tmp, 0, Math.min(s, a.length));
    return tmp;
  }

  /**
   * Copies the specified array.
   * @param a array to be copied
   * @param s new array size
   * @return new array
   */
  public static int[][] copyOf(final int[][] a, final int s) {
    final int[][] tmp = new int[s][];
    System.arraycopy(a, 0, tmp, 0, Math.min(s, a.length));
    return tmp;
  }
  /**
   * Copies the specified array.
   * @param a array to be copied
   * @param s new array size
   * @return new array
   */
  public static String[] copyOf(final String[] a, final int s) {
    final String[] tmp = new String[s];
    System.arraycopy(a, 0, tmp, 0, Math.min(s, a.length));
    return tmp;
  }

  /**
   * Adds an entry to the end of an array and returns the new array.
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
   * Adds an entry to the end of an array and returns the new array.
   * @param ar array to be resized
   * @param e entry to be added
   * @return array
   */
  public static int[] add(final int[] ar, final int e) {
    final int s = ar.length;
    final int[] t = Arrays.copyOf(ar, s + 1);
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
  public static void move(final Object ar, final int pos, final int off, final int l) {
    System.arraycopy(ar, pos, ar, pos + off, l);
  }

  /**
   * Copies entries from one array to another.
   * @param <T> object type
   * @param source source array
   * @param target target array
   * @return object
   */
  public static <T> T[] copy(final T[] source, final T[] target) {
    System.arraycopy(source, 0, target, 0, source.length);
    return target;
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
   * with offsets to the sorted tokens.
   * @param vals values to sort by
   * @param num numeric sort
   * @param asc ascending
   * @return array containing the order
   */
  public static int[] createOrder(final byte[][] vals, final boolean num,
      final boolean asc) {
    final IntList il = number(vals.length);
    il.sort(vals, num, asc);
    return il.toArray();
  }

  /**
   * Sorts the specified doubles and returns an integer array
   * with offsets to the sorted doubles.
   * @param vals values to sort by
   * @param asc ascending
   * @return array containing the order
   */
  public static int[] createOrder(final double[] vals, final boolean asc) {
    final IntList il = number(vals.length);
    il.sort(vals, asc);
    return il.toArray();
  }

  /**
   * Sorts the specified integers and returns an integer array
   * with offsets to the sorted integers.
   * @param vals values to sort by
   * @param asc ascending
   * @return array containing the order
   */
  public static int[] createOrder(final int[] vals, final boolean asc) {
    final IntList il = number(vals.length);
    il.sort(vals, asc);
    return il.toArray();
  }

  /**
   * Returns an integer list with a number list.
   * @param l array size
   * @return number list
   */
  private static IntList number(final int l) {
    final int[] tmp = new int[l];
    for(int i = 0; i < l; ++i) tmp[i] = i;
    return new IntList(tmp);
  }

  /**
   * Reverses the order of the elements in the given array.
   * @param arr array
   */
  public static void reverse(final byte[] arr) {
    reverse(arr, 0, arr.length);
  }

  /**
   * Reverses the order of all elements in the given interval.
   * @param arr array
   * @param pos position of first element of the interval
   * @param len length of the interval
   */
  private static void reverse(final byte[] arr, final int pos, final int len) {
    for(int l = pos, r = pos + len - 1; l < r; l++, r--) {
      final byte tmp = arr[l];
      arr[l] = arr[r];
      arr[r] = tmp;
    }
  }

  /**
   * Reverses the order of all elements in the given interval.
   * @param arr array
   * @param pos position of first element of the interval
   * @param len length of the interval
   */
  public static void reverse(final Object[] arr, final int pos, final int len) {
    for(int l = pos, r = pos + len - 1; l < r; l++, r--) {
      final Object tmp = arr[l];
      arr[l] = arr[r];
      arr[r] = tmp;
    }
  }

  /**
   * Returns a value for a new array size, which will always be larger than
   * the specified value.
   * @param old old size
   * @return resulting size
   */
  public static int newSize(final int old) {
    return newSize(old, RESIZE);
  }

  /**
   * Returns a value for a new array size, which will always be larger than
   * the specified value.
   * @param old old size
   * @param factor resize factor; must be larger than or equal to 1
   * @return resulting size
   */
  public static int newSize(final int old, final double factor) {
    return (int) (old * factor) + 1;
  }

  /**
   * Swaps two entries of the given int array.
   * @param arr array
   * @param a first position
   * @param b second position
   */
  public static void swap(final int[] arr, final int a, final int b) {
    final int c = arr[a];
    arr[a] = arr[b];
    arr[b] = c;
  }

  /**
   * Swaps arr[a .. (a+n-1)] with arr[b .. (b+n-1)].
   * @param arr order array
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  public static void swap(final int[] arr, final int a, final int b,
      final int n) {
    for(int i = 0; i < n; ++i) swap(arr, a + i, b + i);
  }
}
