package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for native integers.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class IntList extends ElementList {
  /** Element container. */
  protected int[] list;

  /**
   * Default constructor.
   */
  public IntList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  public IntList(final int capacity) {
    list = new int[capacity];
  }

  /**
   * Constructor, specifying a resize factor. Smaller values are more memory-saving,
   * while larger will provide better performance.
   * @param resize resize factor
   */
  public IntList(final double resize) {
    this();
    factor = resize;
  }

  /**
   * Lightweight constructor, adopting the specified elements.
   * @param elements initial array
   */
  public IntList(final int[] elements) {
    list = elements;
    size = elements.length;
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public final IntList add(final int element) {
    int[] lst = list;
    int s = size;
    if(s == lst.length) lst = Arrays.copyOf(lst, newSize());
    lst[s++] = element;
    list = lst;
    size = s;
    return this;
  }
  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  public final IntList add(final int... elements) {
    int[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Arrays.copyOf(lst, newSize(ns));
    System.arraycopy(elements, 0, lst, s, l);
    list = lst;
    size = ns;
    return this;
  }

  /**
   * Returns the element at the specified position.
   * @param index index of the element to return
   * @return element
   */
  public final int get(final int index) {
    return list[index];
  }

  /**
   * Stores an element at the specified position.
   * @param index index of the element to replace
   * @param element element to be stored
   */
  public final void set(final int index, final int element) {
    if(index >= list.length) list = Arrays.copyOf(list, newSize(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Checks if the specified element is found in the list.
   * @param element element to be found
   * @return result of check
   */
  public final boolean contains(final int element) {
    final int s = size;
    final int[] lst = list;
    for(int i = 0; i < s; ++i) if(lst[i] == element) return true;
    return false;
  }

  /**
   * Inserts elements at the specified index position.
   * @param index inserting position
   * @param element elements to be inserted
   */
  public final void insert(final int index, final int[] element) {
    final int l = element.length;
    if(l == 0) return;
    if(size + l > list.length) list = Arrays.copyOf(list, newSize(size + l));
    Array.move(list, index, l, size - index);
    System.arraycopy(element, 0, list, index, l);
    size += l;
  }

  /**
   * Removes all occurrences of the specified element from the list.
   * @param element element to be removed
   */
  public final void delete(final int element) {
    final int[] lst = list;
    final int sz = size;
    int s = 0;
    for(int i = 0; i < sz; ++i) {
      if(lst[i] != element) lst[s++] = lst[i];
    }
    size = s;
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   * @return deleted element
   */
  public final int deleteAt(final int index) {
    final int[] lst = list;
    final int l = lst[index];
    Array.move(lst, index + 1, -1, --size - index);
    return l;
  }

  /**
   * Adds a difference to all elements starting from the specified index.
   * @param diff difference
   * @param index index of the first element
   */
  public final void move(final int diff, final int index) {
    final int[] lst = list;
    final int sz = size;
    for(int a = index; a < sz; a++) lst[a] += diff;
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
   * @param element element
   */
  public final void push(final int element) {
    add(element);
  }

  /**
   * Searches the specified element via binary search.
   * Note that all elements must be sorted.
   * @param element element to be found
   * @return index of the search key, or the negative insertion point - 1
   */
  public final int sortedIndexOf(final int element) {
    return Arrays.binarySearch(list, 0, size, element);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final int[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public int[] next() {
    final int[] lst = Arrays.copyOf(list, size);
    reset();
    return lst;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public int[] finish() {
    final int[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
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
  public final void sort(final byte[][] tok, final boolean num, final boolean asc) {
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
  private void sort(final int s, final int e, final boolean g, final boolean f, final byte[][] t) {
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

    final int n = s + e;
    int k = Math.min(a - s, b - a);
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
  private void sort(final int s, final int e, final boolean f, final double[] t) {
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

    final int n = s + e;
    int k = Math.min(a - s, b - a);
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

    final int n = s + e;
    int k = Math.min(a - s, b - a);
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
    return Arrays.toString(toArray());
  }
}
