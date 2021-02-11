package org.basex.util;

import java.util.*;

import org.basex.util.list.*;

/**
 * Convenience functions for handling arrays; serves as an extension to Java's {@link Arrays} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Array {
  /** Maximum array size (see {@code MAX_ARRAY_SIZE} variable in {@link ArrayList}). */
  public static final int MAX_SIZE = Integer.MAX_VALUE - 8;
  /** Initial default size for new arrays. */
  public static final int INITIAL_CAPACITY = 8;
  /** Maximum capacity for new arrays. */
  public static final int MAX_CAPACITY = 1 << 20;
  /** Default factor for resizing dynamic arrays. */
  public static final double RESIZE_CAPACITY = 1.5;

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
    copy(array, Math.min(size, array.length), tmp);
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
    copy(array, Math.min(size, array.length), tmp);
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
    copy(array, Math.min(size, array.length), tmp);
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
   * Inserts entries into an array.
   * @param array array
   * @param index insertion index
   * @param add number of entries to add
   * @param length number of valid array entries
   * @param entries entries to be inserted (can be {@code null})
   */
  public static void insert(final Object array, final int index, final int add, final int length,
      final Object entries) {
    System.arraycopy(array, index, array, index + add, length - index);
    if(entries != null) copyFromStart(entries, add, array, index);
  }

  /**
   * Removes entries inside an array.
   * @param array array
   * @param index index of first entry to be removed
   * @param del number of entries to remove
   * @param length number of valid array entries
   */
  public static void remove(final Object array, final int index, final int del, final int length) {
    System.arraycopy(array, index + del, array, index, length - index - del);
  }

  /**
   * Copies first entries from one array to another array.
   * @param array source array
   * @param index index of source array
   * @param length number of array entries to be copied
   * @param target target array
   * @param trgIndex index of target array
   */
  public static void copy(final Object array, final int index, final int length,
      final Object target, final int trgIndex) {
    System.arraycopy(array, index, target, trgIndex, length);
  }

  /**
   * Copies first entries from one array to another array.
   * @param source source array
   * @param length number of array entries to be copied
   * @param target target array
   */
  public static void copy(final Object source, final int length, final Object target) {
    copyFromStart(source, length, target, 0);
  }

  /**
   * Copies first entries from one array to another array.
   * @param source source array
   * @param length number of array entries to be copied
   * @param target target array
   * @param index target index
   */
  public static void copyFromStart(final Object source, final int length, final Object target,
      final int index) {
    System.arraycopy(source, 0, target, index, length);
  }

  /**
   * Copies first entries from one array to beginning of another array.
   * @param source source array
   * @param index index of first entry to copy
   * @param length number of array entries to be copied
   * @param target target array
   */
  public static void copyToStart(final Object source, final int index, final int length,
      final Object target) {
    System.arraycopy(source, index, target, 0, length);
  }

  /**
   * Copies entries from one array to another array.
   * @param <T> object type
   * @param source source array
   * @param target target array
   * @return object
   */
  public static <T> T[] copy(final T[] source, final T[] target) {
    copy(source, Math.min(source.length, target.length), target);
    return target;
  }

  /**
   * Removes an array entry at the specified position.
   * @param array array to be resized
   * @param index index of entry
   * @param <T> array type
   * @return new array
   */
  public static <T> T[] remove(final T[] array, final int index) {
    final int s = array.length - 1;
    final T[] tmp = Arrays.copyOf(array, s);
    System.arraycopy(array, index + 1, tmp, index, s - index);
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
   * Reverses the order of the entries in the given array.
   * @param array array
   */
  public static void reverse(final byte[] array) {
    reverse(array, 0, array.length);
  }

  /**
   * Reverses the order of all entries in the given interval.
   * @param array array
   * @param index index of first entry of the interval
   * @param length length of the interval
   */
  private static void reverse(final byte[] array, final int index, final int length) {
    for(int l = index, r = index + length - 1; l < r; l++, r--) {
      final byte tmp = array[l];
      array[l] = array[r];
      array[r] = tmp;
    }
  }

  /**
   * Reverses the order of all entries in the given interval.
   * @param array array
   * @param index index of first entry of the interval
   * @param length length of the interval
   */
  public static void reverse(final Object[] array, final int index, final int length) {
    for(int l = index, r = index + length - 1; l < r; l++, r--) {
      final Object tmp = array[l];
      array[l] = array[r];
      array[r] = tmp;
    }
  }

  /**
   * Returns an initial array capacity, which will not exceed {@link #MAX_CAPACITY}.
   * @param size size expected result size (ignored if negative)
   * @return capacity
   */
  public static int initialCapacity(final long size) {
    return size < 0 ? INITIAL_CAPACITY : (int) Math.min(MAX_CAPACITY, size);
  }

  /**
   * Returns a value for a new array size, which will always be larger than the old size.
   * The returned value will not exceed the maximum allowed array size.
   * If the maximum is reached, an exception is thrown.
   * @param size old array capacity
   * @return new capacity
   */
  public static int newCapacity(final int size) {
    return newCapacity(size, RESIZE_CAPACITY);
  }

  /**
   * Returns a value for a new array size, which will always be larger than the old size.
   * The returned value will not exceed the maximum allowed array size.
   * If the maximum is reached, an exception is thrown.
   * @param size old array capacity
   * @param factor resize factor; must be larger than or equal to 1
   * @return new capacity
   */
  public static int newCapacity(final int size, final double factor) {
    return (int) Math.min(MAX_SIZE, factor * checkCapacity(size + 1));
  }

  /**
   * Raises an exception if the specified size exceeds the maximum array size.
   * @param size array capacity
   * @return argument as integer, or {@code 0} if the argument is negative
   */
  public static int checkCapacity(final long size) {
    if(size > MAX_SIZE) throw new ArrayIndexOutOfBoundsException("Maximum array size reached.");
    return Math.max(0, (int) size);
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
