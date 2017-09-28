package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for strings.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class StringList extends ObjectList<String, StringList> {
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
    super(new String[capacity]);
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public StringList(final String... elements) {
    super(elements);
    size = elements.length;
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
   * @param ascending ascending/descending order
   * @param index index of element from which sorting starts
   * @return self reference
   */
  public final StringList sort(final boolean cs, final boolean ascending, final int index) {
    final Comparator<String> comp = cs ? null : String.CASE_INSENSITIVE_ORDER;
    Arrays.sort(list, index, size, ascending ? comp : Collections.reverseOrder(comp));
    return this;
  }

  @Override
  protected final String[] newList(final int s) {
    return new String[s];
  }
}
