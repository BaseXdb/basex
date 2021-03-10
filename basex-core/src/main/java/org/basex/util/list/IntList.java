package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native int values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class IntList extends ElementList {
  /** Element container. */
  protected int[] list;

  /**
   * Default constructor.
   */
  public IntList() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public IntList(final long capacity) {
    list = new int[Array.checkCapacity(capacity)];
  }

  /**
   * Constructor, specifying a resize factor. Smaller values are more memory-saving,
   * while larger values will provide better performance.
   * @param factor resize factor
   */
  public IntList(final double factor) {
    this();
    this.factor = factor;
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
    final int s = size;
    if(s == lst.length) lst = Arrays.copyOf(lst, newCapacity());
    lst[s] = element;
    list = lst;
    size = s + 1;
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
    if(ns > lst.length) lst = Arrays.copyOf(lst, newCapacity(ns));
    Array.copyFromStart(elements, l, lst, s);
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
    if(index >= list.length) list = Arrays.copyOf(list, newCapacity(index + 1));
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
    for(int i = 0; i < s; ++i) {
      if(lst[i] == element) return true;
    }
    return false;
  }

  /**
   * Inserts elements at the specified index position.
   * @param index inserting position
   * @param elements elements to be inserted
   */
  public final void insert(final int index, final int... elements) {
    final int l = elements.length;
    if(l == 0) return;
    if(size + l > list.length) list = Arrays.copyOf(list, newCapacity(size + l));
    Array.insert(list, index, l, size, elements);
    size += l;
  }

  /**
   * Removes all occurrences of the specified element from the list.
   * @param element element to be removed
   */
  public final void removeAll(final int element) {
    final int[] lst = list;
    final int sz = size;
    int s = 0;
    for(int i = 0; i < sz; ++i) {
      if(lst[i] != element) lst[s++] = lst[i];
    }
    size = s;
  }

  /**
   * Removes the element at the specified position.
   * @param index index of the element to remove
   * @return removed element
   */
  public final int remove(final int index) {
    final int[] lst = list;
    final int e = lst[index];
    Array.remove(lst, index, 1, size);
    --size;
    return e;
  }

  /**
   * Adds a difference to all elements starting from the specified index.
   * @param diff difference
   * @param index index of the first element
   */
  public final void incFrom(final int diff, final int index) {
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
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public final int[] finish() {
    final int[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  /**
   * Sorts the data and removes distinct values.
   * @return self reference
   */
  public final IntList ddo() {
    if(!isEmpty()) {
      sort();
      int i = 1;
      for(int j = 1; j < size; ++j) {
        while(j < size && list[i - 1] == list[j]) j++;
        if(j < size) list[i++] = list[j];
      }
      size = i;
    }
    return this;
  }

  /**
   * Reverses the order of the elements.
   * @return self reference
   */
  public IntList reverse() {
    int[] lst = list;
    for(int l = 0, r = size - 1; l < r; l++, r--) {
      final int tmp = lst[l];
      lst[l] = lst[r];
      lst[r] = tmp;
    }
    return this;
  }

  /**
   * Sorts the data and returns an array with offsets to the sorted array.
   * See {@link Array#createOrder(int[], boolean)}
   * @param asc ascending order
   * @return array with new order
   */
  public final int[] createOrder(final boolean asc) {
    final IntList il = Array.number(size);
    il.sort(list, asc);
    return il.finish();
  }

  /**
   * Sorts the data.
   * @return self reference
   */
  public IntList sort() {
    final int s = size;
    if(s > 1) Arrays.sort(list, 0, s);
    return this;
  }

  /**
   * Sorts the data in the order of the specified token array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param values values to sort by
   * @param asc ascending order
   * @param num numeric sort
   */
  public final void sort(final byte[][] values, final boolean asc, final boolean num) {
    sort(values, asc, 0, size, num);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param values values to sort by
   * @param asc ascending order
   */
  public final void sort(final double[] values, final boolean asc) {
    sort(values, asc, 0, size);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param values values to sort by
   * @param asc ascending order
   */
  public final void sort(final int[] values, final boolean asc) {
    sort(values, asc, 0, size);
  }

  /**
   * Sorts the data in the order of the specified numeric array.
   * Note that the input array will be resorted as well.
   * The algorithm is derived from {@link Arrays#sort(int[])}.
   * @param values values to sort by
   * @param asc ascending order
   */
  public final void sort(final long[] values, final boolean asc) {
    sort(values, asc, 0, size);
  }

  /**
   * Sorts the array.
   * @param values values to sort by
   * @param asc ascending/descending order
   * @param start start position
   * @param length length
   * @param num numeric sort
   */
  private void sort(final byte[][] values, final boolean asc, final int start, final int length,
      final boolean num) {
    if(length < 7) {
      for(int i = start; i < length + start; ++i) {
        for(int j = i; j > start; j--) {
          final int h = num ? cmpNum(values[j - 1], values[j]) : cmp(values[j - 1], values[j]);
          if(asc ? h < 0 : h > 0) break;
          swap(j, j - 1, values);
        }
      }
      return;
    }

    int m = start + (length >> 1);
    if(length > 7) {
      int l = start;
      int n = start + length - 1;
      if(length > 40) {
        final int k = length >>> 3;
        l = median(l, l + k, l + (k << 1));
        m = median(m - k, m, m + k);
        n = median(n - (k << 1), n - k, n);
      }
      m = median(l, m, n);
    }
    final byte[] v = values[m];

    int a = start, b = a, c = start + length - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = num ? cmpNum(values[b], v) : cmp(values[b], v);
        if(asc ? h > 0 : h < 0) break;
        if(h == 0) swap(a++, b, values);
        ++b;
      }
      while(c >= b) {
        final int h = num ? cmpNum(values[c], v) : cmp(values[c], v);
        if(asc ? h < 0 : h > 0) break;
        if(h == 0) swap(c, d--, values);
        --c;
      }
      if(b > c) break;
      swap(b++, c--, values);
    }

    final int n = start + length;
    int k = Math.min(a - start, b - a);
    swap(values, start, b - k, k);
    k = Math.min(d - c, n - d - 1);
    swap(values, b, n - k, k);

    if((k = b - a) > 1) sort(values, asc, start, k, num);
    if((k = d - c) > 1) sort(values, asc, n - k, k, num);
  }

  /**
   * Sorts the array.
   * @param values values to sort by
   * @param asc ascending/descending sort
   * @param start start position
   * @param length length
   */
  private void sort(final double[] values, final boolean asc, final int start, final int length) {
    if(length < 7) {
      for(int i = start; i < length + start; ++i) {
        for(int j = i; j > start; j--) {
          final double h = values[j - 1] - values[j];
          if(asc ? h < 0 : h > 0) break;
          swap(j, j - 1, values);
        }
      }
      return;
    }

    int m = start + (length >> 1);
    if(length > 7) {
      int l = start;
      int n = start + length - 1;
      if(length > 40) {
        final int k = length >>> 3;
        l = median(l, l + k, l + (k << 1));
        m = median(m - k, m, m + k);
        n = median(n - (k << 1), n - k, n);
      }
      m = median(l, m, n);
    }
    final double v = values[m];

    int a = start, b = a, c = start + length - 1, d = c;
    while(true) {
      while(b <= c) {
        final double h = values[b] - v;
        if(asc ? h > 0 : h < 0) break;
        if(h == 0) swap(a++, b, values);
        ++b;
      }
      while(c >= b) {
        final double h = values[c] - v;
        if(asc ? h < 0 : h > 0) break;
        if(h == 0) swap(c, d--, values);
        --c;
      }
      if(b > c) break;
      swap(b++, c--, values);
    }

    final int n = start + length;
    int k = Math.min(a - start, b - a);
    swap(values, start, b - k, k);
    k = Math.min(d - c, n - d - 1);
    swap(values, b, n - k, k);

    if((k = b - a) > 1) sort(values, asc, start, k);
    if((k = d - c) > 1) sort(values, asc, n - k, k);
  }

  /**
   * Sorts the array.
   * @param values values to sort by
   * @param asc ascending/descending sort
   * @param start start position
   * @param length length
   */
  private void sort(final int[] values, final boolean asc, final int start, final int length) {
    if(length < 7) {
      for(int i = start; i < start + length; ++i) {
        for(int j = i; j > start; j--) {
          final int h = values[j - 1] - values[j];
          if(asc ? h < 0 : h > 0) break;
          swap(j, j - 1, values);
        }
      }
      return;
    }

    int m = start + (length >> 1);
    if(length > 7) {
      int l = start;
      int n = start + length - 1;
      if(length > 40) {
        final int k = length >>> 3;
        l = median(l, l + k, l + (k << 1));
        m = median(m - k, m, m + k);
        n = median(n - (k << 1), n - k, n);
      }
      m = median(l, m, n);
    }
    final int v = values[m];

    int a = start, b = a, c = start + length - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = values[b] - v;
        if(asc ? h > 0 : h < 0) break;
        if(h == 0) swap(a++, b, values);
        ++b;
      }
      while(c >= b) {
        final int h = values[c] - v;
        if(asc ? h < 0 : h > 0) break;
        if(h == 0) swap(c, d--, values);
        --c;
      }
      if(b > c) break;
      swap(b++, c--, values);
    }

    final int n = start + length;
    int k = Math.min(a - start, b - a);
    swap(values, start, b - k, k);
    k = Math.min(d - c, n - d - 1);
    swap(values, b, n - k, k);

    if((k = b - a) > 1) sort(values, asc, start, k);
    if((k = d - c) > 1) sort(values, asc, n - k, k);
  }

  /**
   * Sorts the array.
   * @param values values to sort by
   * @param asc ascending/descending sort
   * @param start start position
   * @param length length
   */
  private void sort(final long[] values, final boolean asc, final int start, final int length) {
    if(length < 7) {
      for(int i = start; i < length + start; ++i) {
        for(int j = i; j > start; j--) {
          final long h = values[j - 1] - values[j];
          if(asc ? h < 0 : h > 0) break;
          swap(values, j, j - 1);
        }
      }
      return;
    }

    int m = start + (length >> 1);
    if(length > 7) {
      int l = start;
      int n = start + length - 1;
      if(length > 40) {
        final int k = length >>> 3;
        l = median(l, l + k, l + (k << 1));
        m = median(m - k, m, m + k);
        n = median(n - (k << 1), n - k, n);
      }
      m = median(l, m, n);
    }
    final long v = values[m];

    int a = start, b = a, c = start + length - 1, d = c;
    while(true) {
      while(b <= c) {
        final long h = values[b] - v;
        if(asc ? h > 0 : h < 0) break;
        if(h == 0) swap(values, a++, b);
        ++b;
      }
      while(c >= b) {
        final long h = values[c] - v;
        if(asc ? h < 0 : h > 0) break;
        if(h == 0) swap(values, c, d--);
        --c;
      }
      if(b > c) break;
      swap(values, b++, c--);
    }

    final int n = start + length;
    int k = Math.min(a - start, b - a);
    swap(values, start, b - k, k);
    k = Math.min(d - c, n - d - 1);
    swap(values, b, n - k, k);

    if((k = b - a) > 1) sort(values, asc, start, k);
    if((k = d - c) > 1) sort(values, asc, n - k, k);
  }

  /**
   * Compares two numeric tokens and returns an integer.
   * @param value1 first value
   * @param value2 second value
   * @return result
   */
  private static int cmpNum(final byte[] value1, final byte[] value2) {
    final double n = Token.toDouble(value1) - Token.toDouble(value2);
    return n > 0 ? 1 : n < 0 ? -1 : 0;
  }

  /**
   * Compares two tokens and returns an integer.
   * @param value1 first value
   * @param value2 second value
   * @return result
   */
  private static int cmp(final byte[] value1, final byte[] value2) {
    return value1 == null ? value2 == null ? 0 : -1 : value2 == null ? 1 :
      Token.diff(value1, value2);
  }

  /**
   * Swaps two array elements.
   * @param a first offset
   * @param b second offset
   * @param values values to sort by
   */
  private void swap(final int a, final int b, final byte[][] values) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final byte[] c = values[a];
    values[a] = values[b];
    values[b] = c;
  }

  /**
   * Swaps two array elements.
   * @param a first offset
   * @param b second offset
   * @param values values to sort by
   */
  private void swap(final int a, final int b, final double[] values) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final double c = values[a];
    values[a] = values[b];
    values[b] = c;
  }

  /**
   * Swaps two array elements.
   * @param a first offset
   * @param b second offset
   * @param values values to sort by
   */
  private void swap(final int a, final int b, final int[] values) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final int c = values[a];
    values[a] = values[b];
    values[b] = c;
  }

  /**
   * Swaps two array elements.
   * @param values values to sort by
   * @param a first offset
   * @param b second offset
   */
  private void swap(final long[] values, final int a, final int b) {
    final int l = list[a];
    list[a] = list[b];
    list[b] = l;
    final long c = values[a];
    values[a] = values[b];
    values[b] = c;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param values values to sort by
   * @param a first offset
   * @param b second offset
   * @param length number of elements
   */
  private void swap(final byte[][] values, final int a, final int b, final int length) {
    for(int i = 0; i < length; ++i) swap(a + i, b + i, values);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param values values to sort by
   * @param a first offset
   * @param b second offset
   * @param length number of elements
   */
  private void swap(final double[] values, final int a, final int b, final int length) {
    for(int i = 0; i < length; ++i) swap(a + i, b + i, values);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param values values to sort by
   * @param a first offset
   * @param b second offset
   * @param length number of elements
   */
  private void swap(final int[] values, final int a, final int b, final int length) {
    for(int i = 0; i < length; ++i) swap(a + i, b + i, values);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param values values to sort by
   * @param a first offset
   * @param b second offset
   * @param length number of elements
   */
  private void swap(final long[] values, final int a, final int b, final int length) {
    for(int i = 0; i < length; ++i) swap(values, a + i, b + i);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   */
  private int median(final int a, final int b, final int c) {
    return list[a] < list[b] ?
      list[b] < list[c] ? b : list[a] < list[c] ? c : a :
      list[b] > list[c] ? b : list[a] > list[c] ? c : a;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof IntList)) return false;
    final IntList il = (IntList) obj;
    if(size != il.size) return false;
    for(int l = 0; l < size; ++l) {
      if(list[l] != il.list[l]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
