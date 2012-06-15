package org.basex.query.util;

import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This is a light-weight container for XML nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ANodeList {
  /** Element container. */
  private ANode[] list;
  /** Number of elements. */
  private int size;

  /**
   * Constructor.
   */
  public ANodeList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public ANodeList(final int c) {
    list = new ANode[c];
  }

  /**
   * Constructor, specifying initial nodes.
   * @param n initial nodes
   */
  public ANodeList(final ANode... n) {
    list = n;
    size = n.length;
  }

  /**
   * Adds an element to the array.
   * @param e element to be added
   * @return self reference
   */
  public ANodeList add(final ANode e) {
    if(size == list.length) resize(Array.newSize(size));
    list[size++] = e;
    return this;
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final ANode e) {
    if(i >= list.length) resize(Array.newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  public ANode get(final int p) {
    return list[p];
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public int size() {
    return size;
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public Value value() {
    return Seq.get(list, size, NodeType.NOD);
  }

  /**
   * Resizes the array.
   * @param s new size
   */
  private void resize(final int s) {
    final ANode[] tmp = new ANode[s];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }
}
