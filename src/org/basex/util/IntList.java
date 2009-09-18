package org.basex.util;

import java.util.Arrays;

import org.basex.core.Main;

/**
 * This is a simple container for native int values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class IntList {
  /** Value array. */
  protected int[] list;
  /** Number of entries. */
  protected int size;

  /**
   * Default constructor.
   */
  public IntList() {
    this(8);
  }

  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public IntList(final int is) {
    list = new int[is];
  }

  /**
   * Constructor, specifying an initial array.
   * @param v initial list values
   */
  public IntList(final int[] v) {
    list = v;
    size = v.length;
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final int v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Returns the specified value.
   * @param p position
   * @return value
   */
  public int get(final int p) {
    return list[p];
  }

  /**
   * Sets a value at the specified position.
   * @param v value to be added
   * @param p position
   */
  public void set(final int v, final int p) {
    while(p >= list.length) list = Array.extend(list);
    list[p] = v;
    size = Math.max(size, p + 1);
  }

  /**
   * Checks if the specified value is found in the list.
   * @param v value to be added
   * @return true if value is found
   */
  public boolean contains(final int v) {
    for(int i = 0; i < size; i++) if(list[i] == v) return true;
    return false;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public int[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Sorts the data.
   */
  public void sort() {
    Arrays.sort(list, 0, size);
  }

  /**
   * Sorts the data in the order of the specified token array.
   * Note that the input array will be resorted as well. -
   * Sorting is derived from Java's sort algorithms in the {Arrays} class.
   * @param tok token array to sort by
   * @param num numeric sort
   * @param asc ascending
   */
  public void sort(final byte[][] tok, final boolean num, final boolean asc) {
    sort(0, size, num, asc, tok);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well. -
   * Sorting is derived from Java's sort algorithms in the {Arrays} class.
   * @param num token array to sort by
   * @param asc ascending
   */
  public void sort(final double[] num, final boolean asc) {
    sort(0, size, asc, num);
  }

  /**
   * Sorts the array.
   * @param s offset
   * @param e length
   * @param g numeric sort
   * @param f ascending/descending sort
   * @param t sort tokens
   */
  private void sort(final int s, final int e, final boolean g,
      final boolean f, final byte[][] t) {

    if(e < 7) {
      for(int i = s; i < e + s; i++) {
        for(int j = i; j > s; j--) {
          final int h = g ? s(t[j - 1], t[j]) : d(t[j - 1], t[j]);
          if(f ? h < 0 : h > 0) break;
          s(j, j - 1, t);
        }
      }
      return;
    }

    int m = s + (e >> 1);
    if(e > 7) {
      int l = s;
      int n = s + e - 1;
      if(e > 40) {
        final int k = e >>> 3;
        l = m(l, l + k, l + (k << 1));
        m = m(m - k, m, m + k);
        n = m(n - (k << 1), n - k, n);
      }
      m = m(l, m, n);
    }
    final byte[] v = t[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = g ? s(t[b], v) : d(t[b], v);
        if(f ? h > 0 : h < 0) break;
        if(h == 0) s(a++, b, t);
        b++;
      }
      while(c >= b) {
        final int h = g ? s(t[c], v) : d(t[c], v);
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--, t);
        c--;
      }
      if(b > c) break;
      s(b++, c--, t);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(s, b - k, k, t);
    k = Math.min(d - c, n - d - 1);
    s(b, n - k, k, t);

    if((k = b - a) > 1) sort(s, k, g, f, t);
    if((k = d - c) > 1) sort(n - k, k, g, f, t);
  }

  /**
   * Sorts the array.
   * @param s offset
   * @param e length
   * @param f ascending/descending sort
   * @param t sort tokens
   */
  private void sort(final int s, final int e, final boolean f,
      final double[] t) {

    if(e < 7) {
      for(int i = s; i < e + s; i++) {
        for(int j = i; j > s; j--) {
          final double h = t[j - 1] - t[j];
          if(f ? h < 0 : h > 0) break;
          s(j, j - 1, t);
        }
      }
      return;
    }

    int m = s + (e >> 1);
    if(e > 7) {
      int l = s;
      int n = s + e - 1;
      if(e > 40) {
        final int k = e >>> 3;
        l = m(l, l + k, l + (k << 1));
        m = m(m - k, m, m + k);
        n = m(n - (k << 1), n - k, n);
      }
      m = m(l, m, n);
    }
    final double v = t[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final double h = t[b] - v;
        if(f ? h > 0 : h < 0) break;
        if(h == 0) s(a++, b, t);
        b++;
      }
      while(c >= b) {
        final double h = t[c] - v;
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--, t);
        c--;
      }
      if(b > c) break;
      s(b++, c--, t);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(s, b - k, k, t);
    k = Math.min(d - c, n - d - 1);
    s(b, n - k, k, t);

    if((k = b - a) > 1) sort(s, k, f, t);
    if((k = d - c) > 1) sort(n - k, k, f, t);
  }

  /**
   * Compares two numeric tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private int s(final byte[] a, final byte[] b) {
    final double n = Token.toDouble(a) - Token.toDouble(b);
    return n > 0 ? 1 : n < 0 ? -1 : 0;
  }

  /**
   * Compares two tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private int d(final byte[] a, final byte[] b) {
    return a == null ? b == null ? 0 : -1 : b == null ? 1 : Token.diff(a, b);
  }

  /**
   * Swaps two array values.
   * @param a first offset
   * @param b second offset
   * @param t sort tokens
   */
  private void s(final int a, final int b, final byte[][] t) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final byte[] c = t[a];
    t[a] = t[b];
    t[b] = c;
  }

  /**
   * Swaps two array values.
   * @param a first offset
   * @param b second offset
   * @param t sort tokens
   */
  private void s(final int a, final int b, final double[] t) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final double c = t[a];
    t[a] = t[b];
    t[b] = c;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int n, final byte[][] t) {
    for(int i = 0; i < n; i++) s(a + i, b + i, t);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int n, final double[] t) {
    for(int i = 0; i < n; i++) s(a + i, b + i, t);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   */
  private int m(final int a, final int b, final int c) {
    return list[a] < list[b] ?
      (list[b] < list[c] ? b : list[a] < list[c] ? c : a) :
      list[b] > list[c] ? b : list[a] > list[c] ? c : a;
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder(Main.name(this) + '[');
    for(int i = 0; i < size; i++) sb.add((i == 0 ? "" : ",") + list[i]);
    return sb.add(']').toString();
  }
}
