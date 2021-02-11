package org.basex.query.util.fingertree;

import java.util.*;

import org.basex.util.*;

/**
 * List iterator over the elements of a finger tree.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <E> element type
 */
final class FingerTreeIterator<E> implements ListIterator<E> {
  /** Size of the root. */
  private final long n;
  /** Current index. */
  private long index;

  /** Stack of deep trees. */
  private DeepTree<?, E>[] trees;
  /** Position of the current node in the digits. */
  private int deepPos;
  /** Stack pointer for the deep trees. */
  private int tTop = -1;

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
   * Constructor for iterating over a single node.
   * @param root root node
   * @param start starting position
   */
  @SuppressWarnings("unchecked")
  FingerTreeIterator(final Node<?, E> root, final long start) {
    n = root.size();
    index = start;

    if(root instanceof InnerNode) {
      nodes = new InnerNode[8];
      poss = new int[8];
      nTop = 0;
      nodes[0] = (InnerNode<?, E>) root;
    } else {
      leaf = (Node<E, E>) root;
      leafPos = (int) start;
      nTop = -1;
    }

    assert start >= 0 && start <= n;
  }

  /**
   * Constructor for iterating over a deep fingertree.
   * @param tree finger tree
   * @param start start position
   */
  @SuppressWarnings("unchecked")
  FingerTreeIterator(final DeepTree<?, E> tree, final long start) {
    n = tree.size();
    index = start;

    trees = new DeepTree[8];
    trees[0] = tree;
    tTop = 0;

    nodes = new InnerNode[8];
    poss = new int[8];
    nTop = -1;
  }

  /**
   * Returns a list iterator for the given finger tree starting at the given position.
   * @param <E> element type
   * @param tree finger tree
   * @param start starting position
   * @return the iterator
   */
  static <E> ListIterator<E> get(final FingerTree<?, E> tree, final long start) {
    if(tree.isEmpty()) return Collections.emptyListIterator();
    if(tree instanceof SingletonTree) return new FingerTreeIterator<>(tree.head(), start);
    return new FingerTreeIterator<>((DeepTree<?, E>) tree, start);
  }

  /**
   * Initializes this iterator by descending to the correct leaf node.
   */
  @SuppressWarnings("unchecked")
  private void init() {
    Node<?, E> node;
    long pos = Math.min(index, n - 1);

    if(tTop >= 0) {
      while(true) {
        final DeepTree<?, E> curr = trees[tTop];
        if(pos < curr.leftSize) {
          // left digit
          final Node<?, E>[] left = curr.left;
          int i = 0;
          for(;; i++) {
            node = left[i];
            final long size = node.size();
            if(pos < size) break;
            pos -= size;
          }
          deepPos = i - left.length;
          break;
        }
        pos -= curr.leftSize;

        final FingerTree<?, E> mid = curr.middle;
        final long midSize = mid.size();
        if(pos >= midSize) {
          // right digit
          pos -= midSize;
          final Node<?, E>[] right = curr.right;
          int i = 0;
          for(;; i++) {
            node = right[i];
            final long size = node.size();
            if(pos < size) break;
            pos -= size;
          }
          deepPos = i + 1;
          break;
        }

        if(mid instanceof SingletonTree) {
          // single middle node
          node = mid.head();
          deepPos = 0;
          break;
        }

        // go one level deeper
        if(++tTop == trees.length) trees = Arrays.copyOf(trees, 2 * tTop);
        trees[tTop] = (DeepTree<?, E>) mid;
      }
    } else {
      deepPos = 0;
      node = nodes[0];
      nTop = -1;
    }

    Node<?, E> curr = node;
    while(curr instanceof InnerNode) {
      final InnerNode<?, E> inner = (InnerNode<?, E>) curr;

      int idx = 0;
      Node<?, E> sub = inner.getSub(0);
      while(true) {
        final long size = sub.size();
        if(pos < size) break;
        pos -= size;
        sub = inner.getSub(++idx);
      }

      if(++nTop == nodes.length) {
        nodes = Arrays.copyOf(nodes, 2 * nTop);
        poss = Arrays.copyOf(poss, 2 * nTop);
      }
      nodes[nTop] = inner;
      poss[nTop] = idx;
      curr = sub;
    }

    leaf = (Node<E, E>) curr;
    leafPos = (int) (index < n ? pos : pos + 1);
  }

  @Override
  public int nextIndex() {
    return (int) index;
  }

  @Override
  public boolean hasNext() {
    return index < n;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E next() {
    if(leaf == null) init();

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
      final DeepTree<?, E> curr = trees[tTop];
      if(deepPos < -1) {
        // go to next node in digit
        ++deepPos;
        start = curr.left[curr.left.length + deepPos];
      } else if(deepPos == -1) {
        // left digit drained
        final FingerTree<?, E> mid = curr.middle;
        if(mid instanceof EmptyTree) {
          // skip empty middle tree
          deepPos = 1;
          start = curr.right[0];
        } else if(mid instanceof SingletonTree) {
          // iterate through the one middle node
          deepPos = 0;
          start = ((SingletonTree<?, E>) mid).elem;
        } else {
          final DeepTree<?, E> deep = (DeepTree<?, E>) mid;
          if(++tTop == trees.length) {
            final DeepTree<?, E>[] newTrees = new DeepTree[2 * tTop];
            Array.copy(trees, tTop, newTrees);
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
        Array.copy(nodes, nTop, newNodes);
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
    if(leaf == null) init();

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
      final DeepTree<?, E> curr = trees[tTop];
      if(deepPos > 1) {
        // go to next node in right digit
        --deepPos;
        start = curr.right[deepPos - 1];
      } else if(deepPos == 1) {
        // right digit drained
        final FingerTree<?, E> mid = curr.middle;
        if(mid instanceof EmptyTree) {
          // skip empty middle tree
          final int l = curr.left.length;
          start = curr.left[l - 1];
          deepPos = -1;
        } else if(mid instanceof SingletonTree) {
          // iterate through the one middle node
          start = ((SingletonTree<?, E>) mid).elem;
          deepPos = 0;
        } else {
          // go into the middle tree
          final DeepTree<?, E> deep = (DeepTree<?, E>) mid;
          if(++tTop == trees.length) {
            final DeepTree<?, E>[] newTrees = new DeepTree[2 * tTop];
            Array.copy(trees, tTop, newTrees);
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
        Array.copy(nodes, nTop, newNodes);
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
    throw Util.notExpected();
  }

  @Override
  public void add(final E e) {
    throw Util.notExpected();
  }

  @Override
  public void remove() {
    throw Util.notExpected();
  }
}
