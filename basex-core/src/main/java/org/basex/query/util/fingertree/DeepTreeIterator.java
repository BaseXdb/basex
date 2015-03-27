package org.basex.query.util.fingertree;

import java.util.*;

/**
 * Iterator over a finger tree with at least two sub-nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <E> element type
 */
final class DeepTreeIterator<E> implements ListIterator<E> {
  /** Current index. */
  private long index;

  /** Stack of deep trees. */
  private Deep<?, E>[] trees;
  /** Position of the current node in the digits. */
  private int deepPos;
  /** Stack pointer for the deep trees. */
  private int tTop;

  /** Node stack. */
  private InnerNode<?, E>[] nodes;
  /** Position stack. */
  private int[] poss;
  /** Stack pointer. */
  private int nTop;

  /** Current leaf node. */
  private Node<E, E> leaf;
  /** Position inside the current leaf. */
  private int leafPos;

  /**
   * Constructor.
   * @param root root node
   * @param reverse flag for starting at the end
   */
  @SuppressWarnings("unchecked")
  DeepTreeIterator(final Deep<?, E> root, final boolean reverse) {
    index = reverse ? root.size : 0;

    trees = new Deep[8];
    trees[0] = root;
    tTop = 0;

    nodes = new InnerNode[8];
    poss = new int[8];
    nTop = -1;

    Node<?, E> curr = reverse ? root.right[root.right.length - 1] : root.left[0];
    while(curr instanceof InnerNode) {
      final InnerNode<?, E> inner = (InnerNode<?, E>) curr;
      final int idx = reverse ? inner.arity() - 1 : 0;
      if(++nTop == nodes.length) {
        nodes = Arrays.copyOf(nodes, 2 * nodes.length);
        poss = Arrays.copyOf(poss, 2 * poss.length);
      }
      nodes[nTop] = inner;
      poss[nTop] = idx;
      curr = inner.getSub(idx);
    }

    deepPos = reverse ? root.right.length : -root.left.length;
    leaf = (Node<E, E>) curr;
    leafPos = reverse ? curr.arity() : 0;
  }

  @Override
  public int nextIndex() {
    return (int) index;
  }

  @Override
  public boolean hasNext() {
    return index < trees[0].size;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E next() {
    if(index >= trees[0].size) throw new NoSuchElementException();

    index++;
    if(leafPos < leaf.arity()) return leaf.getSub(leafPos++);

    // leaf drained, backtrack
    while(nTop >= 0 && poss[nTop] == nodes[nTop].arity() - 1) nTop--;

    final Node<?, E> start;
    if(nTop >= 0) {
      // go to next sub-node
      start = nodes[nTop].getSub(++poss[nTop]);
    } else {
      // node drained, move to the next one
      final Deep<?, E> curr = trees[tTop];
      if(deepPos < -1) {
        // go to next node in digit
        ++deepPos;
        start = curr.left[curr.left.length + deepPos];
      } else if(deepPos == -1) {
        // left digit drained
        final FingerTree<?, E> mid = curr.middle;
        if(mid instanceof Empty) {
          // skip empty middle tree
          deepPos = 1;
          start = curr.right[0];
        } else if(mid instanceof Single) {
          // iterate through the one middle node
          deepPos = 0;
          start = ((Single<?, E>) mid).elem;
        } else {
          final Deep<?, E> deep = (Deep<?, E>) mid;
          if(++tTop == trees.length) {
            final Deep<?, E>[] newTrees = new Deep[2 * tTop];
            System.arraycopy(trees, 0, newTrees, 0, tTop);
            trees = newTrees;
          }
          trees[tTop] = deep;
          deepPos = -deep.left.length;
          start = deep.left[0];
        }
      } else if(deepPos == 0) {
        // we are in a single middle node
        deepPos = 1;
        start = curr.right[0];
      } else {
        // we are in the right digit
        final int p = deepPos - 1;
        if(p < curr.right.length - 1) {
          // go to next node in digit
          deepPos++;
          start = curr.right[p + 1];
        } else {
          // backtrack one level
          trees[tTop] = null;
          deepPos = 0;
          tTop--;
          start = trees[tTop].right[0];
          deepPos = 1;
        }
      }
    }

    Node<?, E> sub = start;
    while(sub instanceof InnerNode) {
      if(++nTop == nodes.length) {
        final InnerNode<?, E>[] newNodes = new InnerNode[2 * nTop];
        System.arraycopy(nodes, 0, newNodes, 0, nTop);
        nodes = newNodes;
        poss = Arrays.copyOf(poss, 2 * nTop);
      }
      nodes[nTop] = (InnerNode<?, E>) sub;
      poss[nTop] = 0;
      sub = nodes[nTop].getSub(0);
    }

    leaf = (Node<E, E>) sub;
    leafPos = 1;
    return leaf.getSub(0);
  }

  @Override
  public int previousIndex() {
    return (int) (index - 1);
  }

  @Override
  public boolean hasPrevious() {
    return index > 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E previous() {
    if(index <= 0) throw new NoSuchElementException();

    --index;
    if(leafPos > 0) return leaf.getSub(--leafPos);

    // leaf drained, backtrack
    while(nTop >= 0 && poss[nTop] == 0) nTop--;

    final Node<?, E> start;
    if(nTop >= 0) {
      // go to previous sub-node
      start = nodes[nTop].getSub(--poss[nTop]);
    } else {
      // node drained, move to the previous one
      final Deep<?, E> curr = trees[tTop];
      if(deepPos > 1) {
        // go to next node in right digit
        --deepPos;
        start = curr.right[deepPos - 1];
      } else if(deepPos == 1) {
        // right digit drained
        final FingerTree<?, E> mid = curr.middle;
        if(mid instanceof Empty) {
          // skip empty middle tree
          final int l = curr.left.length;
          start = curr.left[l - 1];
          deepPos = -1;
        } else if(mid instanceof Single) {
          // iterate through the one middle node
          start = ((Single<?, E>) mid).elem;
          deepPos = 0;
        } else {
          // go into the middle tree
          final Deep<?, E> deep = (Deep<?, E>) mid;
          if(++tTop == trees.length) {
            final Deep<?, E>[] newTrees = new Deep[2 * tTop];
            System.arraycopy(trees, 0, newTrees, 0, tTop);
            trees = newTrees;
          }
          trees[tTop] = deep;
          final int r = deep.right.length;
          start = deep.right[r - 1];
          deepPos = r;
        }
      } else if(deepPos == 0) {
        start = curr.left[curr.left.length - 1];
        deepPos = -1;
      } else {
        // we are in the left digit
        final int l = curr.left.length, p = l + deepPos;
        if(p > 0) {
          // go to previous node in digit
          --deepPos;
          start = curr.left[p - 1];
        } else {
          // backtrack one level
          trees[tTop] = null;
          --tTop;
          final Node<?, E>[] left = trees[tTop].left;
          start = left[left.length - 1];
          deepPos = -1;
        }
      }
    }

    Node<?, E> sub = start;
    while(sub instanceof InnerNode) {
      if(++nTop == nodes.length) {
        final InnerNode<?, E>[] newNodes = new InnerNode[2 * nTop];
        System.arraycopy(nodes, 0, newNodes, 0, nTop);
        nodes = newNodes;
        poss = Arrays.copyOf(poss, 2 * nTop);
      }
      nodes[nTop] = (InnerNode<?, E>) sub;
      poss[nTop] = sub.arity() - 1;
      sub = nodes[nTop].getSub(poss[nTop]);
    }

    leaf = (Node<E, E>) sub;
    leafPos = sub.arity() - 1;
    return leaf.getSub(leafPos);
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
