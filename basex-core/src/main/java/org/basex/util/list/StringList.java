package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for strings.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * @param elements elements to be added
   * @return self reference
   */
  public final StringList add(final String... elements) {
    for(final String s : elements) add(s);
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
      while(0 != (result = list[i].compareTo(e))) {
        if(++i >= size() || result > 0) return false;
      }
    }
    return true;
  }

  /**
   * Deletes the specified element.
   * @param index index of element to be deleted
   */
  public final void deleteAt(final int index) {
    Array.move(list, index + 1, -1, --size - index);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final String[] toArray() {
    return Array.copyOf(list, size);
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
   * Removes duplicates from the list.
   * The list must be sorted.
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

  /**
   * Returns the uppermost element from the stack.
   * @return the uppermost element
   */
  public final String peek() {
    return list[size - 1];
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public final String pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public final void push(final String element) {
    add(element);
  }

  @Override
  public final Iterator<String> iterator() {
    return new ArrayIterator<String>(list, size);
  }

  @Override
  public final String toString() {
    return Arrays.toString(toArray());
  }
}
