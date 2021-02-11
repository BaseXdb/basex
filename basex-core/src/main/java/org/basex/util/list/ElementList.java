package org.basex.util.list;

import org.basex.util.*;

/**
 * This is an abstract class for storing elements of any kind in an array-based list.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ElementList {
  /** Resize factor for extending arrays. */
  protected double factor = Array.RESIZE_CAPACITY;
  /** Number of elements. */
  protected int size;

  /**
   * Default constructor.
   */
  protected ElementList() { }

  /**
   * Returns a new array size.
   * @return new array size
   */
  protected final int newCapacity() {
    return Array.newCapacity(size, factor);
  }

  /**
   * Returns a new array size that is larger than or equal to the specified size.
   * @param min minimum size
   * @return new array size
   */
  protected final int newCapacity(final int min) {
    return Math.max(newCapacity(), min);
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public final int size() {
    return size;
  }

  /**
   * Enforces the number of elements.
   * @param sz number of elements
   */
  public final void size(final int sz) {
    size = sz;
  }

  /**
   * Tests whether the container has no elements.
   * @return result of check
   */
  public final boolean isEmpty() {
    return size == 0;
  }

  /**
   * Resets the array size.
   */
  public final void reset() {
    size = 0;
  }
}
