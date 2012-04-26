package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for native integers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class IntList extends ElementList {
  /** Element container. */
  protected int[] list;

  /**
   * Default constructor.
   */
  public IntList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public IntList(final int c) {
    list = new int[c];
  }

  /**
   * Constructor.
   * @param f resize factor
   */
  public IntList(final double f) {
    this();
    factor = f;
  }

  /**
   * Constructor, specifying an initial array.
   * @param a initial array
   */
  public IntList(final int[] a) {
    list = a;
    size = a.length;
  }

  /**
   * Adds an entry to the array.
   * @param e entry to be added
   */
  public final void add(final int e) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = e;
  }

  /**
   * Returns the element at the specified index position.
   * @param i index
   * @return element
   */
  public final int get(final int i) {
    return list[i];
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public final void set(final int i, final int e) {
    if(i >= list.length) list = Arrays.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public final boolean contains(final int e) {
    for(int i = 0; i < size; ++i) if(list[i] == e) return true;
    return false;
  }

  /**
   * Inserts elements at the specified index position.
   * @param i index
   * @param e elements to be inserted
   */
  public final void insert(final int i, final int[] e) {
    final int l = e.length;
    if(l == 0) return;
    if(size + l > list.length) list = Arrays.copyOf(list, newSize(size + l));
    Array.move(list, i, l, size - i);
    System.arraycopy(e, 0, list, i, l);
    size += l;
  }

  /**
   * Removes the specified element from the list.
   * @param e element to be removed
   */
  public final void delete(final int e) {
    int s = 0;
    for(int i = 0; i < size; ++i) {
      if(list[i] != e) list[s++] = list[i];
    }
    size = s;
  }

  /**
   * Deletes the element at the specified position.
   * @param i position to delete
   */
  public final void deleteAt(final int i) {
    Array.move(list, i + 1, -1, --size - i);
  }

  /**
   * Adds a difference to all elements starting from the specified index.
   * @param e difference
   * @param i index
   */
  public final void move(final int e, final int i) {
    for(int a = i; a < size; a++) list[a] += e;
  }

  /**
   * Returns the uppermost element from the stack.
   * @return the uppermost element
   */
  public final int peek() {
    return list[size - 1];
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public final int pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param val element
   */
  public final void push(final int val) {
    add(val);
  }

  /**
   * Searches the specified element via binary search.
   * Note that all elements must be sorted.
   * @param e element to be found
   * @return index of the search key, or the negative insertion point - 1
   */
  public final int sortedIndexOf(final int e) {
    return Arrays.binarySearch(list, 0, size, e);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final int[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Sorts the data.
   * @return self reference
   */
  public IntList sort() {
    Arrays.sort(list, 0, size);
    return this;
  }

  /**
   * Sorts the data in the order of the specified token array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param tok token array to sort by
   * @param num numeric sort
   * @param asc ascending
   */
  public final void sort(final byte[][] tok, final boolean num,
      final boolean asc) {
    sort(0, size, num, asc, tok);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param num token array to sort by
   * @param asc ascending
   */
  public final void sort(final double[] num, final boolean asc) {
    sort(0, size, asc, num);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param num token array to sort by
   * @param asc ascending
   */
  public final void sort(final int[] num, final boolean asc) {
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
      for(int i = s; i < e + s; ++i) {
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
        ++b;
      }
      while(c >= b) {
        final int h = g ? s(t[c], v) : d(t[c], v);
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--, t);
        --c;
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
      for(int i = s; i < e + s; ++i) {
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
        ++b;
      }
      while(c >= b) {
        final double h = t[c] - v;
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--, t);
        --c;
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
   * Sorts the array.
   * @param s offset
   * @param e length
   * @param f ascending/descending sort
   * @param t sort tokens
   */
  private void sort(final int s, final int e, final boolean f, final int[] t) {
    if(e < 7) {
      for(int i = s; i < e + s; ++i) {
        for(int j = i; j > s; j--) {
          final int h = t[j - 1] - t[j];
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
    final int v = t[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = t[b] - v;
        if(f ? h > 0 : h < 0) break;
        if(h == 0) s(a++, b, t);
        ++b;
      }
      while(c >= b) {
        final int h = t[c] - v;
        if(f ? h < 0 : h > 0) break;
        if(h == 0) s(c, d--, t);
        --c;
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
  private static int s(final byte[] a, final byte[] b) {
    final double n = Token.toDouble(a) - Token.toDouble(b);
    return n > 0 ? 1 : n < 0 ? -1 : 0;
  }

  /**
   * Compares two tokens and returns an integer.
   * @param a first token
   * @param b second token
   * @return result
   */
  private static int d(final byte[] a, final byte[] b) {
    return a == null ? b == null ? 0 : -1 : b == null ? 1 : Token.diff(a, b);
  }

  /**
   * Swaps two array elements.
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
   * Swaps two array elements.
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
   * Swaps two array elements.
   * @param a first offset
   * @param b second offset
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int[] t) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final int c = t[a];
    t[a] = t[b];
    t[b] = c;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of elements
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int n, final byte[][] t) {
    for(int i = 0; i < n; ++i) s(a + i, b + i, t);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of elements
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int n, final double[] t) {
    for(int i = 0; i < n; ++i) s(a + i, b + i, t);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of elements
   * @param t sort tokens
   */
  private void s(final int a, final int b, final int n, final int[] t) {
    for(int i = 0; i < n; ++i) s(a + i, b + i, t);
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
      list[b] < list[c] ? b : list[a] < list[c] ? c : a :
      list[b] > list[c] ? b : list[a] > list[c] ? c : a;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.name(this) + '[');
    for(int i = 0; i < size; ++i) tb.add((i == 0 ? "" : ", ") + list[i]);
    return tb.add(']').toString();
  }
}
