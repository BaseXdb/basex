package org.basex.util;

import java.util.*;

import org.basex.util.list.*;

/**
 * Convenience functions for handling arrays; serves as an extension to Java's {@link Arrays} class.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Array {
  /** Maximum size for initializing arrays. */
  public static final int MAXINIT = 1 << 20;
  /** Initial default size for new arrays. */
  public static final int CAPACITY = 1 << 3;
  /** Default factor for resizing dynamic arrays. */
  public static final double RESIZE = 1.5;
  /** Private constructor. */
  private Array() { }

  /**
   * Copies the specified array.
   * @param array array to be copied
   * @param size new array size
   * @return new array
   */
  public static byte[][] copyOf(final byte[][] array, final int size) {
    final byte[][] tmp = new byte[size][];
    System.arraycopy(array, 0, tmp, 0, Math.min(size, array.length));
    return tmp;
  }

  /**
   * Copies the specified array.
   * @param array array to be copied
   * @param size new array size
   * @return new array
   */
  public static int[][] copyOf(final int[][] array, final int size) {
    final int[][] tmp = new int[size][];
    System.arraycopy(array, 0, tmp, 0, Math.min(size, array.length));
    return tmp;
  }

  /**
   * Copies the specified array.
   * @param array array to be copied
   * @param size new array size
   * @return new array
   */
  public static String[] copyOf(final String[] array, final int size) {
    final String[] tmp = new String[size];
    System.arraycopy(array, 0, tmp, 0, Math.min(size, array.length));
    return tmp;
  }


  /**
   * Adds an entry to the end of an array and returns the new array.
   * @param array array to be resized
   * @param entry entry to be added
   * @param <T> array type
   * @return array
   */
  public static <T> T[] add(final T[] array, final T entry) {
    final int s = array.length;
    final T[] t = Arrays.copyOf(array, s + 1);
    t[s] = entry;
    return t;
  }

  /**
   * Adds an entry to the end of an array and returns the new array.
   * @param array array to be resized
   * @param entry entry to be added
   * @return array
   */
  public static int[] add(final int[] array, final int entry) {
    final int s = array.length;
    final int[] t = Arrays.copyOf(array, s + 1);
    t[s] = entry;
    return t;
  }

  /**
   * Moves entries inside an array.
   * @param array array
   * @param pos position
   * @param off move offset
   * @param size number of entries to move
   */
  public static void move(final Object array, final int pos, final int off, final int size) {
    System.arraycopy(array, pos, array, pos + off, size);
  }

  /**
   * Copies entries from one array to another.
   * @param <T> object type
   * @param source source array
   * @param target target array
   * @return object
   */
  public static <T> T[] copy(final T[] source, final T[] target) {
    System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
    return target;
  }

  /**
   * Removes an array entry at the specified position.
   * @param array array to be resized
   * @param pos position
   * @param <T> array type
   * @return new array
   */
  public static <T> T[] delete(final T[] array, final int pos) {
    final int s = array.length - 1;
    final T[] tmp = Arrays.copyOf(array, s);
    System.arraycopy(array, pos + 1, tmp, pos, s - pos);
    return tmp;
  }

  /**
   * Sorts the specified tokens and returns an array with offsets to the sorted array.
   * @param values values to sort by (will be sorted as well)
   * @param numeric numeric sort
   * @param ascending ascending
   * @return array containing the order
   */
  public static int[] createOrder(final byte[][] values, final boolean numeric,
      final boolean ascending) {
    final IntList il = number(values.length);
    il.sort(values, numeric, ascending);
    return il.finish();
  }

  /**
   * Sorts the specified double values and returns an array with offsets to the sorted array.
   * @param values values to sort by (will be sorted as well)
   * @param ascending ascending
   * @return array containing the order
   */
  public static int[] createOrder(final double[] values, final boolean ascending) {
    final IntList il = number(values.length);
    il.sort(values, ascending);
    return il.finish();
  }

  /**
   * Sorts the specified int values and returns an array with offsets to the sorted array.
   * @param values values to sort by (will be sorted as well)
   * @param ascending ascending
   * @return array containing the order
   */
  public static int[] createOrder(final int[] values, final boolean ascending) {
    final IntList il = number(values.length);
    il.sort(values, ascending);
    return il.finish();
  }

  /**
   * Sorts the specified long values and returns an array with offsets to the sorted array.
   * @param values values to sort by (will be sorted as well)
   * @param ascending ascending
   * @return array containing the order
   */
  public static int[] createOrder(final long[] values, final boolean ascending) {
    final IntList il = number(values.length);
    il.sort(values, ascending);
    return il.finish();
  }

  /**
   * Returns an enumerated integer list.
   * @param size array size
   * @return number list
   */
  public static IntList number(final int size) {
    final int[] tmp = new int[size];
    for(int i = 0; i < size; ++i) tmp[i] = i;
    return new IntList(tmp);
  }

  /**
   * Reverses the order of the elements in the given array.
   * @param array array
   */
  public static void reverse(final byte[] array) {
    reverse(array, 0, array.length);
  }

  /**
   * Reverses the order of all elements in the given interval.
   * @param array array
   * @param pos position of first element of the interval
   * @param len length of the interval
   */
  private static void reverse(final byte[] array, final int pos, final int len) {
    for(int l = pos, r = pos + len - 1; l < r; l++, r--) {
      final byte tmp = array[l];
      array[l] = array[r];
      array[r] = tmp;
    }
  }

  /**
   * Reverses the order of all elements in the given interval.
   * @param array array
   * @param pos position of first element of the interval
   * @param len length of the interval
   */
  public static void reverse(final Object[] array, final int pos, final int len) {
    for(int l = pos, r = pos + len - 1; l < r; l++, r--) {
      final Object tmp = array[l];
      array[l] = array[r];
      array[r] = tmp;
    }
  }

  /**
   * Returns a value for a new array size, which will always be larger than the specified value.
   * @param old old size
   * @return resulting size
   */
  public static int newSize(final int old) {
    return newSize(old, RESIZE);
  }

  /**
   * Returns a value for a new array size, which will always be larger than the specified value.
   * @param old old size
   * @param factor resize factor; must be larger than or equal to 1
   * @return resulting size
   */
  public static int newSize(final int old, final double factor) {
    return (int) (old * factor) + 1;
  }

  /**
   * Compares two token arrays for equality.
   * @param tokens1 first tokens (can be {@code null})
   * @param tokens2 second tokens (can be {@code null})
   * @return result of check
   */
  public static boolean equals(final byte[][] tokens1, final byte[][] tokens2) {
    if(tokens1 == tokens2) return true;
    if(tokens1 == null || tokens2 == null) return false;
    final int al = tokens1.length;
    if(al != tokens2.length) return false;
    for(int a = 0; a < al; a++) {
      if(!Token.eq(tokens1[a], tokens2[a])) return false;
    }
    return true;
  }

  /**
   * Compares two arrays for equality.
   * @param arr1 first array (can be {@code null})
   * @param arr2 second array (can be {@code null})
   * @return result of check
   */
  public static boolean equals(final Object[] arr1, final Object[] arr2) {
    if(arr1 == arr2) return true;
    if(arr1 == null || arr2 == null) return false;
    final int al = arr1.length;
    if(al != arr2.length) return false;
    for(int a = 0; a < al; a++) {
      if(!Objects.equals(arr1[a], arr2[a])) return false;
    }
    return true;
  }
}
