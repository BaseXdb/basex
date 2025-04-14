package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for strings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringList extends ObjectList<String, StringList> {
  /**
   * Default constructor.
   */
  public StringList() {
    this(-1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public StringList(final long capacity) {
    super(new String[Array.initialCapacity(capacity)]);
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
   * Adds a token.
   * @param element element to be added
   * @return self reference
   */
  public StringList add(final byte[] element) {
    return add(Token.string(element));
  }

  /**
   * Sorts the elements in ascending order, using the standard options.
   * @return self reference
   */
  public StringList sort() {
    return sort(true);
  }

  /**
   * Sorts the elements in ascending order.
   * @param cs respect case sensitivity
   * @return self reference
   */
  public StringList sort(final boolean cs) {
    return sort(cs, true);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending/descending flag
   * @return self reference
   */
  public StringList sort(final boolean cs, final boolean asc) {
    final Comparator<String> comp = cs ? null : String.CASE_INSENSITIVE_ORDER;
    Arrays.sort(list, 0, size, asc ? comp : Collections.reverseOrder(comp));
    return this;
  }

  @Override
  protected String[] newArray(final int s) {
    return new String[s];
  }
}
