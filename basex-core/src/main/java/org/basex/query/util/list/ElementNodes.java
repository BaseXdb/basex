package org.basex.query.util.list;

import java.util.*;

/**
 * Linked element list.
 * @param <E> element type
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ElementNodes<E> implements Iterable<E> {
  /** First entry. */
  private Node<E> first;
  /** Last entry. */
  private Node<E> last;

  /**
   * Adds an entry.
   * @param entry entry to be added
   */
  public void add(final E entry) {
    final Node<E> node = new Node<>();
    node.entry = entry;
    if(first == null) {
      first = node;
    } else {
      last.next = node;
    }
    node.prev = last;
    last = node;
  }

  @Override
  public NodeIterator iterator() {
    return new NodeIterator(first);
  }

  /**
   * Deletes a node.
   * @param node node to be deleted
   * @return next node
   */
  private Node<E> delete(final Node<E> node) {
    final Node<E> n = node.next, p = node.prev;
    if(p != null) {
      p.next = n;
    } else {
      first = node;
    }
    if(n != null) {
      n.prev = p;
    } else {
      last = node;
    }
    return n;
  }

  /**
   * List node.
   * @param <E> element type
   */
  static class Node<E> {
    /** Entry. */
    E entry;
    /** Next node. */
    Node<E> next;
    /** Previous node. */
    Node<E> prev;
  }

  /**
   * List iterator.
   */
  public class NodeIterator implements Iterator<E> {
    /** Initial node. */
    private Node<E> init;
    /** Current node. */
    private Node<E> curr;

    /**
     * Constructor.
     * @param node node to start from
     */
    NodeIterator(final Node<E> node) {
      init = node;
    }

    @Override
    public boolean hasNext() {
      if(curr == null) {
        curr = init;
        init = null;
      } else {
        curr = curr.next;
      }
      return curr != null;
    }

    @Override
    public E next() {
      return curr.entry;
    }

    @Override
    public void remove() {
      curr = delete(curr);
    }

    /**
     * Creates a copy of the iterator.
     * @return copy
     */
    public NodeIterator copy() {
      return new NodeIterator(curr);
    }
  }
}
