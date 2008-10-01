package org.basex.util;

/**
 * This is a simple container for native int values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IntList {
  /** Value array. */
  private int[] list;
  /** Current array size. */
  public int size;

  /**
   * Default constructor.
   */
  public IntList() {
    list = new int[8];
  }
  
  /**
   * Constructor, specifying an initial value.
   * @param v initial list values
   */
  public IntList(final int v) {
    this(new int[] { v });
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
   * Adds next value.
   * @param v value to be added
   */
  public void add(final IntList v) {
    if(size == list.length) list = Array.extend(list);
    for (int i = 0; i < v.size; i++)
      list[size++] = v.get(i);
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
    for(int i = 0; i < size; i++) if(list[i] == v) return true;
    return false;
  }
  
  /**
   * Removes the element at the specified position in this list. 
   * Shifts any subsequent elements to the left (subtracts one 
   * from their indices). 
   * @param index - the index of the element to removed.
   * @return the element that was removed from the list.
   */
  public int remove(final int index) {
    if(size == 0 || size < index) throw new IndexOutOfBoundsException();
    final int elem = list[index];
    System.arraycopy(list, index + 1, list, index, size - index);
    --size;
    return elem;
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
   * Resets the integer list.
   * @param is initial size
   */
  public void reset(final int is) {
    size = 0;
    list = new int[is];
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
    if(size > 1) sort(0, size, num, asc, tok);
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("IntList[");
    for(int i = 0; i < size; i++) sb.append((i == 0 ? "" : ",") + list[i]);
    return sb.append("]").toString();
  }
}
