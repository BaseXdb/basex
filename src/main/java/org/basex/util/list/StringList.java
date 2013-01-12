package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for strings.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class StringList extends ElementList implements Iterable<String> {
  /** Element container. */
  protected String[] list;

  /**
   * Default constructor.
   */
  public StringList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public StringList(final int c) {
    list = new String[c];
  }

  /**
   * Constructor, specifying an initial array.
   * @param l initial array
   */
  public StringList(final String[] l) {
    list = Array.copyOf(l, l.length);
    size = l.length;
  }

  /**
   * Adds an element to the array.
   * @param e element to be added
   * @return self reference
   */
  public final StringList add(final String e) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = e;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param e element to be added
   * @return self reference
   */
  public final StringList add(final String[] e) {
    for(final String s : e) add(s);
    return this;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public final String get(final int p) {
    return list[p];
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public final void set(final int i, final String e) {
    if(i >= list.length) list = Arrays.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public final boolean contains(final String e) {
    for(int i = 0; i < size; ++i) if(list[i].equals(e)) return true;
    return false;
  }

  /**
   * Check if other list is fully contained in this list. Both lists must be sorted!
   * @param l sorted list
   * @return is l contained in this list?
   */
  public final boolean containsAll(final StringList l) {
    if (isEmpty() && !l.isEmpty()) return false;
    int i = 0;
    for (String e : l) {
      int result;
      while (0 != (result = list[i].compareTo(e))) {
        if (++i >= size() || result > 0) return false;
      }
    }
    return true;
  }

  /**
   * Deletes the specified element.
   * @param i element to be deleted
   */
  public final void deleteAt(final int i) {
    Array.move(list, i + 1, -1, --size - i);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final String[] toArray() {
    return Array.copyOf(list, size);
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
   * @param asc ascending/descending flag
   * @param pos position where sorting starts
   * @return self reference
   */
  public final StringList sort(final boolean cs, final boolean asc, final int pos) {
    final Comparator<String> comp = cs ? null : String.CASE_INSENSITIVE_ORDER;
    Arrays.sort(list, pos, size, asc ? comp : Collections.reverseOrder(comp));
    return this;
  }

  /**
   * Removes duplicates from the list.
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
    return new Iterator<String>() {
      private int c;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public String next() { return list[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public final String toString() {
    return Arrays.toString(toArray());
  }
}
