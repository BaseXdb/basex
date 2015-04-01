package org.basex.query.util.fingertree;

import java.util.*;

/**
 * A builder for {@link FingerTree}s from leaf nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <E> element type
 */
@SuppressWarnings("unchecked")
public final class FingerTreeBuilder<E> {
  /** The root node, {@code null} if the tree is empty. */
  private Object root;

  /**
   * Checks if this builder is empty, i.e. if no leaf nodes were added to it.
   * @return {@code true} if the builder is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return root == null;
  }

  /**
   * Adds a leaf node to the front of the tree.
   * @param leaf the leaf node to add
   */
  public void prepend(final Node<E, E> leaf) {
    if(root == null) {
      root = new BufferNode<>(leaf);
    } else if(root instanceof BufferNode) {
      ((BufferNode<E, E>) root).prepend(leaf);
    } else {
      final BufferNode<E, E> newRoot = new BufferNode<>((FingerTree<E, E>) root);
      newRoot.prepend(leaf);
      root = newRoot;
    }
  }

  /**
   * Adds a leaf node to the back of the tree.
   * @param leaf the leaf node to add
   */
  public void append(final Node<E, E> leaf) {
    if(root == null) {
      root = new BufferNode<>(leaf);
    } else if(root instanceof BufferNode) {
      ((BufferNode<E, E>) root).append(leaf);
    } else {
      final BufferNode<E, E> newRoot = new BufferNode<>((FingerTree<E, E>) root);
      newRoot.append(leaf);
      root = newRoot;
    }
  }

  /**
   * Appends another finger tree to this builder.
   * @param tree finger tree to append
   */
  public void append(final FingerTree<E, E> tree) {
    if(!tree.isEmpty()) {
      if(root == null) {
        root = new BufferNode<>(tree);
      } else if(root instanceof BufferNode) {
        ((BufferNode<E, E>) root).append(tree);
      } else {
        final BufferNode<E, E> newRoot = new BufferNode<>((FingerTree<E, E>) root);
        newRoot.append(tree);
        root = newRoot;
      }
    }
  }

  /**
   * Builds a finger tree from the current state of this builder.
   * @return the resulting finger tree
   */
  public FingerTree<E, E> freeze() {
    return root == null ? FingerTree.<E>empty() :
      root instanceof BufferNode ? ((BufferNode<E, E>) root).freeze() : (FingerTree<E, E>) root;
  }

  /**
   * Writes the elements contained in this builder onto the given string builder.
   * @param sb string builder
   */
  public void toString(final StringBuilder sb) {
    if(root != null) {
      if(root instanceof BufferNode) {
        ((BufferNode<E, E>) root).toString(sb);
      } else {
        boolean first = true;
        for(final E e : (FingerTree<E, E>) root) {
          if(!first) sb.append(", ");
          else first = false;
          sb.append(e);
        }
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    toString(sb);
    return sb.append(']').toString();
  }

  /**
   * Node of the middle tree.
   *
   * @param <N> node type
   * @param <E> element type
   */
  private static class BufferNode<N, E> {
    /** Maximum number of nodes in the digits. */
    private static final int CAP = 1 << 3;
    /** Mask for calculating positions in the ring buffer. */
    private static final int MASK = CAP - 1;
    /** Ring buffer for nodes in the digits. */
    final Node<N, E>[] nodes = new Node[CAP];
    /** Number of elements in left digit. */
    int inLeft;
    /** Position of middle between left and right digit in buffer. */
    int midPos = 4;
    /** Number of elements in right digit. */
    int inRight;
    /**
     * Root node of middle tree, either a {@code FingerTree<Node<N, E>, E>} or a
     * {@code BufferNode<Node<N, E>, E>}.
     */
    Object middle;

    /**
     * Constructs a buffered tree containing the given single node.
     * @param node the initial node
     */
    BufferNode(final Node<N, E> node) {
      prepend(node);
    }

    /**
     * Constructs a buffered tree containing the same contents as the given tree.
     * @param tree the tree to take the contents of
     */
    BufferNode(final FingerTree<N, E> tree) {
      if(tree instanceof Single) {
        prepend(((Single<N, E>) tree).elem);
      } else {
        final Deep<N, E> deep = (Deep<N, E>) tree;
        for(int i = deep.left.length; --i >= 0;) prepend(deep.left[i]);
        final FingerTree<Node<N, E>, E> mid = deep.middle;
        if(!mid.isEmpty()) middle = mid;
        for(final Node<N, E> node : deep.right) append(node);
      }
    }

    /**
     * Adds a node to the front of this tree.
     * @param node the node to add
     */
    void prepend(final Node<N, E> node) {
      if(inLeft < 4) {
        nodes[(midPos - inLeft - 1 + CAP) & MASK] = node;
        inLeft++;
      } else if(middle == null && inRight < 4) {
        midPos = (midPos - 1 + CAP) & MASK;
        nodes[(midPos - inLeft + CAP) & MASK] = node;
        inRight++;
      } else {
        final int l3 = (midPos - 1 + CAP) & MASK, l2 = (l3 - 1 + CAP) & MASK,
            l1 = (l2 - 1 + CAP) & MASK, l0 = (l1 - 1 + CAP) & MASK;
        final Node<Node<N, E>, E> next = new InnerNode3<>(nodes[l1], nodes[l2], nodes[l3]);
        nodes[l3] = nodes[l0];
        nodes[l2] = node;
        nodes[l1] = null;
        nodes[l0] = null;
        inLeft = 2;
        if(middle == null) middle = new BufferNode<>(next);
        else midBuffer().prepend(next);
      }
    }

    /**
     * Adds a node to the back of this tree.
     * @param node the node to add
     */
    void append(final Node<N, E> node) {
      if(inRight < 4) {
        nodes[(midPos + inRight) & MASK] = node;
        inRight++;
      } else if(middle == null && inLeft < 4) {
        midPos = (midPos + 1) & MASK;
        nodes[(midPos + inRight - 1) & MASK] = node;
        inLeft++;
      } else {
        final int r0 = midPos, r1 = (r0 + 1) & MASK, r2 = (r1 + 1) & MASK, r3 = (r2 + 1) & MASK;
        final Node<Node<N, E>, E> next = new InnerNode3<>(nodes[r0], nodes[r1], nodes[r2]);
        nodes[r0] = nodes[r3];
        nodes[r1] = node;
        nodes[r2] = null;
        nodes[r3] = null;
        inRight = 2;
        if(middle == null) middle = new BufferNode<>(next);
        else midBuffer().append(next);
      }
    }

    /**
     * Appends the contents of the given tree to this buffer.
     * @param tree finger tree to append
     */
    void append(final FingerTree<N, E> tree) {
      if(!(tree instanceof Deep)) {
        if(tree instanceof Single) append(((Single<N, E>) tree).elem);
        return;
      }

      final Deep<N, E> deep = (Deep<N, E>) tree;
      final Node<N, E>[] ls = deep.left, rs = deep.right;
      final int ll = ls.length, rl = rs.length;
      final FingerTree<Node<N, E>, E> mid = deep.middle;

      if(mid.isEmpty()) {
        for(int i = 0; i < ll; i++) append(ls[i]);
        for(int i = 0; i < rl; i++) append(rs[i]);
      } else if(middle == null) {
        final int n = inLeft + inRight;
        final Node<N, E>[] buff = new Node[n + ll];
        for(int i = 0; i < n; i++) buff[i] = nodes[(midPos - inLeft + i + CAP) & MASK];
        System.arraycopy(ls, 0, buff, n, ll);
        inLeft = inRight = 0;
        middle = mid;
        for(int i = buff.length; --i >= 0;) prepend(buff[i]);
        for(int i = 0; i < rl; i++) append(rs[i]);
      } else {
        final int k = inRight + ll;
        final Node<N, E>[] buff = new Node[k];
        for(int i = 0; i < inRight; i++) {
          final int j = (midPos + i) & MASK;
          buff[i] = nodes[j];
          nodes[j] = null;
        }
        System.arraycopy(ls, 0, buff, inRight, ll);
        inRight = 0;

        for(int i = 0; i < k;) {
          final int rest = k - i;
          if(rest > 4 || rest == 3) {
            final Node<Node<N, E>, E> sub = new InnerNode3<>(buff[i], buff[i + 1], buff[i + 2]);
            if(middle == null) middle = new BufferNode<>(sub);
            else midBuffer().append(sub);
            i += 3;
          } else {
            final Node<Node<N, E>, E> sub = new InnerNode2<>(buff[i], buff[i + 1]);
            if(middle == null) middle = new BufferNode<>(sub);
            else midBuffer().append(sub);
            i += 2;
          }
        }

        if(middle == null) middle = mid;
        else midBuffer().append(mid);

        for(int i = 0; i < rl; i++) append(rs[i]);
      }
    }

    /**
     * Returns the middle tree as a buffer node.
     * @return middle buffer node
     */
    private BufferNode<Node<N, E>, E> midBuffer() {
      if(middle == null) return null;
      if(middle instanceof BufferNode) return (BufferNode<Node<N, E>, E>) middle;
      final BufferNode<Node<N, E>, E> mid = new BufferNode<>((FingerTree<Node<N, E>, E>) middle);
      middle = mid;
      return mid;
    }

    /**
     * Creates an {@link FingerTree} containing the elements of this builder.
     * @return the finger tree
     */
    FingerTree<N, E> freeze() {
      final int n = inLeft + inRight;
      if(n == 1) return new Single<>(nodes[(midPos + inRight - 1 + CAP) & MASK]);
      final int a = middle == null ? n / 2 : inLeft, b = n - a;
      final Node<N, E>[] left = new Node[a], right = new Node[b];
      final int lOff = midPos - inLeft + CAP, rOff = lOff + a;
      for(int i = 0; i < a; i++) left[i]  = nodes[(lOff + i) & MASK];
      for(int i = 0; i < b; i++) right[i] = nodes[(rOff + i) & MASK];

      if(middle == null) return Deep.get(left, right);

      if(middle instanceof FingerTree) {
        final FingerTree<Node<N, E>, E> tree = (FingerTree<Node<N, E>, E>) middle;
        return Deep.get(left, tree, right);
      }

      final BufferNode<Node<N, E>, E> buffer = (BufferNode<Node<N, E>, E>) middle;
      return Deep.get(left, buffer.freeze(), right);
    }

    /**
     * Writes the elements contained in this node onto the given string builder.
     * @param sb string builder
     */
    void toString(final StringBuilder sb) {
      boolean first = true;
      for(int i = 0; i < inLeft; i++) {
        final Node<N, E> node = nodes[(midPos - inLeft + i + CAP) & MASK];
        for(final E elem : node) {
          if(first) first = false;
          else sb.append(", ");
          sb.append(elem);
        }
      }
      if(!(middle == null)) {
        if(middle instanceof BufferNode) {
          ((BufferNode<?, ?>) middle).toString(sb.append(", "));
        } else {
          final FingerTree<?, ?> tree = (FingerTree<?, ?>) middle;
          final Iterator<?> iter = tree.iterator();
          while(iter.hasNext()) sb.append(", ").append(iter.next());
        }
      }
      for(int i = 0; i < inRight; i++) {
        final Node<N, E> node = nodes[(midPos + i) & MASK];
        for(final E elem : node) {
          if(first) first = false;
          else sb.append(", ");
          sb.append(elem);
        }
      }
    }
  }
}
