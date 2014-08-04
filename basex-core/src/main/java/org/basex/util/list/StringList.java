package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for strings.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class StringList extends ElementList implements Iterable<String> {
  /** Element container. */
  protected String[] list;

  /**
   * Default constructor.
   */
  public StringList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  public StringList(final int capacity) {
    list = new String[capacity];
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public StringList(final String... elements) {
    list = elements;
    size = elements.length;
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public final StringList add(final String element) {
    String[] lst = list;
    int s = size;
    if(s == lst.length) lst = Array.copyOf(lst, newSize());
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
  public final StringList add(final String... elements) {
    String[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Array.copyOf(lst, newSize(ns));
    System.arraycopy(elements, 0, lst, s, l);
    list = lst;
    size = ns;
    return this;
  }

  /**
   * Adds elements from a string list to the array.
   * @param elements string list to be added
   * @return self reference
   */
  public final StringList add(final StringList elements) {
    for(final String s : elements) add(s);
    return this;
  }

  /**
   * Returns the element at the specified position.
   * @param index element index
   * @return element
   */
  public final String get(final int index) {
    return list[index];
  }

  /**
   * Sets an element at the specified index position.
   * @param index index
   * @param element element to be set
   */
  public final void set(final int index, final String element) {
    if(index >= list.length) list = Array.copyOf(list, newSize(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Checks if the specified element is found in the list.
   * @param element element to be found
   * @return result of check
   */
  public final boolean contains(final String element) {
    for(int i = 0; i < size; ++i) if(list[i].equals(element)) return true;
    return false;
  }

  /**
   * Check if all elements of the specified list are contained in the list.
   * Both lists must be sorted.
   * @param elements sorted list
   * @return result of check
   */
  public final boolean containsAll(final StringList elements) {
    if(isEmpty() && !elements.isEmpty()) return false;
    int i = 0;
    for(final String e : elements) {
      int result;
      while((result = list[i].compareTo(e)) != 0) {
        if(++i >= size() || result > 0) return false;
      }
    }
    return true;
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   * @return deleted element
   */
  public final String deleteAt(final int index) {
    final String l = list[index];
    Array.move(list, index + 1, -1, --size - index);
    return l;
  }

  /**
   * Removes all occurrences of the specified element from the list.
   * @param element element to be removed
   */
  public final void delete(final String element) {
    final String[] lst = list;
    final int sz = size;
    int s = 0;
    for(int i = 0; i < sz; ++i) {
      if(!lst[i].equals(element)) lst[s++] = lst[i];
    }
    size = s;
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final String[] toArray() {
    return Array.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public final String[] next() {
    final String[] lst = Array.copyOf(list, size);
    reset();
    return lst;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public String[] finish() {
    final String[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Array.copyOf(lst, s);
  }

  /**
   * Sorts the elements in ascending order, using the standard options.
   * @return self reference
   */
  public final StringList sort() {
    return sort(true);
  }

  /**
   * Sorts the elements in ascending order.
   * @param cs respect case sensitivity
   * @return self reference
   */
  public final StringList sort(final boolean cs) {
    return sort(cs, true, 0);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending/descending flag
   * @return self reference
   */
  public final StringList sort(final boolean cs, final boolean asc) {
    return sort(cs, asc, 0);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending (true)/descending (false) flag
   * @param index index of element from which sorting starts
   * @return self reference
   */
  public final StringList sort(final boolean cs, final boolean asc, final int index) {
    final Comparator<String> comp = cs ? null : String.CASE_INSENSITIVE_ORDER;
    Arrays.sort(list, index, size, asc ? comp : Collections.reverseOrder(comp));
    return this;
  }

  /**
   * Removes duplicates, provided that the entries are sorted.
   * @return self reference
   */
  public StringList unique() {
    if(size != 0) {
      int s = 0;
      for(int l = 1; l < size; l++) {
        if(!list[l].equals(list[s])) list[++s] = list[l];
      }
      size = s + 1;
    }
    return this;
  }

  @Override
  public final Iterator<String> iterator() {
    return new ArrayIterator<>(list, size);
  }

  @Override
  public final String toString() {
    return Arrays.toString(toArray());
  }
}
