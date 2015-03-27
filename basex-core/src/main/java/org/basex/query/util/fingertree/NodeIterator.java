package org.basex.query.util.fingertree;

import java.util.*;

/**
 * An {@link ListIterator} over the elements in a node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <E> element type
 */
public final class NodeIterator<E> implements ListIterator<E> {
  /** Node stack. */
  private InnerNode<?, E>[] nodes;
  /** Position stack. */
  private int[] poss;
  /** Stack pointer. */
  private int top;
  /** Current leaf node. */
  private Node<E, E> leaf;
  /** Position inside the current leaf. */
  private int leafPos;

  /** Current index. */
  private long index;
  /** Number of elements in the root node. */
  private long rootSize;

  /**
   * Constructor.
   * @param start root node
   * @param reverse flag for starting at the back of the tree
   */
  @SuppressWarnings("unchecked")
  NodeIterator(final Node<?, E> start, final boolean reverse) {
    nodes = new InnerNode[8];
    poss = new int[8];
    rootSize = start.size();
    index = reverse ? rootSize : 0;
    top = -1;
    Node<?, E> curr = start;
    while(curr instanceof InnerNode) {
      final InnerNode<?, E> inner = (InnerNode<?, E>) curr;
      final int idx = reverse ? inner.arity() - 1 : 0;
      if(++top == nodes.length) {
        InnerNode<?, E>[] newNodes = new InnerNode[2 * top];
        System.arraycopy(nodes, 0, newNodes, 0, top);
        nodes = newNodes;
        int[] newPoss = new int[2 * top];
        System.arraycopy(poss, 0, newPoss, 0, top);
        poss = newPoss;
      }
      nodes[top] = inner;
      poss[top] = idx;
      curr = inner.getSub(idx);
    }
    leaf = (Node<E, E>) curr;
    leafPos = reverse ? curr.arity() : 0;
  }

  @Override
  public boolean hasNext() {
    return index < rootSize;
  }

  @Override
  public int nextIndex() {
    return (int) index;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E next() {
    if(index >= rootSize) throw new NoSuchElementException();
    index++;
    if(leafPos >= leaf.arity()) {
      while(poss[top] >= nodes[top].arity() - 1) top--;
      poss[top]++;
      Node<?, E> sub = nodes[top].getSub(poss[top]);
      while(sub instanceof InnerNode) {
        ++top;
        nodes[top] = (InnerNode<?, E>) sub;
        poss[top] = 0;
        sub = nodes[top].getSub(0);
      }
      leaf = (Node<E, E>) sub;
      leafPos = 0;
    }
    return leaf.getSub(leafPos++);
  }

  @Override
  public boolean hasPrevious() {
    return index > 0;
  }

  @Override
  public int previousIndex() {
    return (int) (index - 1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public E previous() {
    if(index <= 0) throw new NoSuchElementException();
    index--;
    if(leafPos <= 0) {
      while(poss[top] <= 0) top--;
      poss[top]--;
      Node<?, E> sub = nodes[top].getSub(poss[top]);
      while(sub instanceof InnerNode) {
        ++top;
        nodes[top] = (InnerNode<?, E>) sub;
        poss[top] = sub.arity() - 1;
        sub = nodes[top].getSub(poss[top]);
      }
      leaf = (Node<E, E>) sub;
      leafPos = sub.arity();
    }
    return leaf.getSub(--leafPos);
  }

  @Override
  public void set(final E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(final E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
