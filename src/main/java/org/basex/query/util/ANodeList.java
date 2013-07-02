package org.basex.query.util;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a light-weight container for XML nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ANodeList extends ElementList implements Iterable<ANode> {
  /** Element container. */
  ANode[] list;

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
   */
  public void add(final ANode e) {
    if(size == list.length) copyOf(newSize());
    list[size++] = e;
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final ANode e) {
    if(i >= list.length) copyOf(newSize(i + 1));
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
  private void copyOf(final int s) {
    final ANode[] tmp = new ANode[s];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  @Override
  public Iterator<ANode> iterator() {
    return new ArrayIterator<ANode>(list, size);
  }
}
