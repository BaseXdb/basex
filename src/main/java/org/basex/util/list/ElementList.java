package org.basex.util.list;

import org.basex.util.Array;

/**
 * This is an abstract class for storing elements of any kind in an array-based
 * list.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ElementList {
  /** Initial hash capacity. */
  public static final int CAP = 1 << 3;
  /** Resize factor for extending the arrays. */
  double factor = Array.RESIZE;
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
  protected final int newSize() {
    return Array.newSize(size, factor);
  }

  /**
   * Returns a new array size that is larger than or equal to the specified
   * size.
   * @param min minimum size
   * @return new array size
   */
  final int newSize(final int min) {
    return Math.max(newSize(), min);
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public final int size() {
    return size;
  }

  /**
   * Sets the number of elements to the specified value.
   * @param s number of elements
   */
  public final void size(final int s) {
    size = s;
  }

  /**
   * Tests is the container has no elements.
   * @return result of check
   */
  public final boolean empty() {
    return size == 0;
  }

  /**
   * Resets the array size.
   */
  public final void reset() {
    size = 0;
  }
}
