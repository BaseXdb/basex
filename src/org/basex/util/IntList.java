package org.basex.util;

/**
 * This is a simple container for native int values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IntList {
  /** Value array. */
  private int[] list = new int[8];
  /** Iterator. */
  private int pos = -1;
  /** Current array size. */
  public int size;

  /**
   * Default constructor.
   */
  public IntList() { }
  
  /**
   * Default constructor.
   * @param v initial list value
   */
  public IntList(final int v) {
    list[0] = v;
    size = 1;
  }

  /**
   * Default constructor.
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
   * Returns the value at the specified position.
   * @param p position
   * @return value
   */
  public int get(final int p) {
    return list[p];
  }

  /**
   * Returns the value array.
   * @return value
   */
  public int[] get() {
    return list;
  }

  /**
   * Checks if the specified value is found in the list.
   * @param v value to be added
   * @return true if value is found
   */
  public boolean contains(final int v) {
    for(int i = 0; i < size; i++) {
      if(list[i] == v) return true;
    }
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
    init();
  }

  /**
   * Initializes the iterator.
   */
  public void init() {
    pos = -1;
  }

  /**
   * Checks if the iterator offers more values.
   * @return true/false
   */
  public boolean more() {
    return ++pos < size;
  }

  /**
   * Returns next iterated value.
   * @return next value
   */
  public int next() {
    return list[pos];
  }

  /**
   * Sorts the array in the order of the specified token array.
   * This sort algorithm is derived from Java's highly optimized
   * {Arrays#sort} array sort algorithms.
   * @param tok token array to sort by
   * @param num numeric sort
   * @param asc ascending
   */
  public void sort(final byte[][] tok, final boolean num, final boolean asc) {
    t = tok;
    if(size > 1) sort(0, size, num, asc);
    t = null;
  }

  /** Temporary token array. */
  private byte[][] t;

  /**
   * Sorts the array.
   * @param s offset
   * @param e length
   * @param g numeric sort
   * @param f ascending/descending sort
   */
  private void sort(final int s, final int e, final boolean g,
      final boolean f) {

    if(e < 7) {
      for(int i = s; i < e + s; i++) {
        for(int j = i; j > s; j--) {
          final int h = g ? s(t[j - 1], t[j]) : Token.diff(t[j - 1], t[j]);
          if(f ? h < 0 : h > 0) break;
          s(j, j - 1);
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
        final int h = g ? s(t[b], v) : Token.diff(t[b], v);
        if(f ? h > 0 : h < 0) break;
        if(h == 0) s(a++, b);
        b++;
      }
      while(c >= b) {
        final int h = g ? s(t[c], v) : Token.diff(t[c], v);
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--);
        c--;
      }
      if(b > c) break;
      s(b++, c--);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(s, b - k, k);
    k = Math.min(d - c, n - d - 1);
    s(b, n - k, k);

    if((k = b - a) > 1) sort(s, k, g, f);
    if((k = d - c) > 1) sort(n - k, k, g, f);
  }

  /**
   * Compares two numeric tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private int s(final byte[] a, final byte[] b) {
    final long n = Token.toLong(a) - Token.toLong(b);
    return n > 0 ? 1 : n < 0 ? -1 : 0;
  }
    
  /**
   * Swaps two array values.
   * @param a first offset
   * @param b second offset
   */
  private void s(final int a, final int b) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final byte[] c = t[a];
    t[a] = t[b];
    t[b] = c;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  private void s(final int a, final int b, final int n) {
    for(int i = 0; i < n; i++) s(a + i, b + i);
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
        (list[b] > list[c] ? b : list[a] > list[c] ? c : a);
  }
}
