package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for native booleans.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BoolList extends ElementList {
  /** Element container. */
  private boolean[] list;

  /**
   * Default constructor.
   */
  public BoolList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  private BoolList(final int capacity) {
    list = new boolean[capacity];
  }

  /**
   * Adds an element.
   * @param element element to be added
   */
  public void add(final boolean element) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = element;
  }

  /**
   * Returns the element at the specified index.
   * @param index index of the element to return
   * @return element
   */
  public boolean get(final int index) {
    return list[index];
  }

  /**
   * Stores an element at the specified index.
   * @param index index of the element to replace
   * @param element element to be stored
   */
  public void set(final int index, final boolean element) {
    if(index >= list.length) list = Arrays.copyOf(list, newSize(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public boolean pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public void push(final boolean element) {
    add(element);
  }

  /**
   * Returns the uppermost element on the stack, without removing it.
   * @return uppermost element
   */
  public boolean peek() {
    return list[size - 1];
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public boolean[] toArray() {
    return Arrays.copyOf(list, size);
  }

  @Override
  public String toString() {
    return Arrays.toString(toArray());
  }
}
