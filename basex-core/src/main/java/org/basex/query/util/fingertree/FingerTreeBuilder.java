package org.basex.query.util.fingertree;

import java.util.*;

import org.basex.util.*;

/**
 * A builder for {@link FingerTree}s from leaf nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <E> element type
 */
@SuppressWarnings("unchecked")
public final class FingerTreeBuilder<E> implements Iterable<E> {
  /** The root node, {@code null} if the tree is empty. */
  private BufferNode<E, E> root;

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
    } else {
      root.prepend(leaf);
    }
  }

  /**
   * Adds a leaf node to the back of the tree.
   * @param leaf the leaf node to add
   */
  public void append(final Node<E, E> leaf) {
    if(root == null) {
      root = new BufferNode<>(leaf);
    } else {
      root.append(leaf);
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
      } else {
        root.append(tree);
      }
    }
  }

  /**
   * Builds a finger tree from the current state of this builder.
   * @return the resulting finger tree
   */
  public FingerTree<E, E> freeze() {
    return root == null ? FingerTree.empty() : root.freeze();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    final Iterator<E> iter = iterator();
    if(iter.hasNext()) {
      sb.append(iter.next());
      while(iter.hasNext()) sb.append(", ").append(iter.next());
    }
    return sb.append(']').toString();
  }

  @Override
  public Iterator<E> iterator() {
    if(root == null) return Collections.emptyIterator();
    return new BufferNodeIterator<>(root);
  }

  /**
   * Node of the middle tree.
   *
   * @param <N> node type
   * @param <E> element type
   */
  private static class BufferNode<N, E> {
    /** Size of inner nodes to create. */
    private static final int NODE_SIZE = FingerTree.MAX_ARITY;
    /** Maximum number of elements in a digit. */
    private static final int MAX_DIGIT = NODE_SIZE + 1;
    /** Maximum number of nodes in the digits. */
    private static final int CAP = 2 * MAX_DIGIT;
    /** Ring buffer for nodes in the digits. */
    final Node<N, E>[] nodes = new Node[CAP];
    /** Number of elements in left digit. */
    int inLeft;
    /** Position of middle between left and right digit in buffer. */
    int midPos = MAX_DIGIT;
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
      if(tree instanceof SingletonTree) {
        prepend(((SingletonTree<N, E>) tree).elem);
      } else {
        final DeepTree<N, E> deep = (DeepTree<N, E>) tree;
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
      if(inLeft < MAX_DIGIT) {
        nodes[(midPos - inLeft - 1 + CAP) % CAP] = node;
        inLeft++;
      } else if(middle == null && inRight < MAX_DIGIT) {
        midPos = (midPos - 1 + CAP) % CAP;
        nodes[(midPos - inLeft + CAP) % CAP] = node;
        inRight++;
      } else {
        final int l = (midPos - inLeft + CAP) % CAP;
        final Node<Node<N, E>, E> next = new InnerNode<>(copy(l + 1, inLeft - 1));
        nodes[(midPos - 1 + CAP) % CAP] = nodes[l];
        nodes[(midPos - 2 + CAP) % CAP] = node;
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
      if(inRight < MAX_DIGIT) {
        nodes[(midPos + inRight) % CAP] = node;
        inRight++;
      } else if(middle == null && inLeft < MAX_DIGIT) {
        midPos = (midPos + 1) % CAP;
        nodes[(midPos + inRight - 1) % CAP] = node;
        inLeft++;
      } else {
        final Node<Node<N, E>, E> next = new InnerNode<>(copy(midPos, inRight - 1));
        nodes[midPos] = nodes[(midPos + inRight - 1) % CAP];
        nodes[(midPos + 1) % CAP] = node;
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
      if(!(tree instanceof DeepTree)) {
        if(tree instanceof SingletonTree) append(((SingletonTree<N, E>) tree).elem);
        return;
      }

      final DeepTree<N, E> deep = (DeepTree<N, E>) tree;
      final Node<N, E>[] ls = deep.left, rs = deep.right;
      final int ll = ls.length;
      final FingerTree<Node<N, E>, E> mid = deep.middle;

      if(mid.isEmpty()) {
        // add digits
        for(final Node<N, E> l : ls) append(l);
        for(final Node<N, E> r : rs) append(r);
      } else if(middle == null) {
        // cache previous contents and re-add them afterwards
        final int n = inLeft + inRight;
        final Node<N, E>[] buff = new Node[n + ll];
        copyInto(midPos - inLeft, buff, 0, n);
        Array.copyFromStart(ls, ll, buff, n);
        inLeft = inRight = 0;
        middle = mid;
        for(int i = buff.length; --i >= 0;) prepend(buff[i]);
        for(final Node<N, E> r : rs) append(r);
      } else {
        // inner digits have to be merged
        final int n = inRight + ll;
        final Node<N, E>[] buff = new Node[n];
        copyInto(midPos, buff, 0, inRight);
        Array.copyFromStart(ls, ll, buff, inRight);
        inRight = 0;
        for(int k = (n + NODE_SIZE - 1) / NODE_SIZE, p = 0; k > 0; k--) {
          final int inNode = (n - p + k - 1) / k;
          final Node<N, E>[] out = new Node[inNode];
          Array.copyToStart(buff, p, inNode, out);
          final Node<Node<N, E>, E> sub = new InnerNode<>(out);
          if(middle == null) middle = new BufferNode<>(sub);
          else midBuffer().append(sub);
          p += inNode;
        }
        if(middle == null) middle = mid;
        else midBuffer().append(mid);
        for(final Node<N, E> r : rs) append(r);
      }
    }

    /**
     * Creates an {@link FingerTree} containing the elements of this builder.
     * @return the finger tree
     */
    FingerTree<N, E> freeze() {
      final int n = inLeft + inRight;
      if(n == 1) return new SingletonTree<>(nodes[(midPos + inRight - 1 + CAP) % CAP]);
      final int a = middle == null ? n / 2 : inLeft, l = midPos - inLeft;
      final Node<N, E>[] left = copy(l, a), right = copy(l + a, n - a);
      if(middle == null) return DeepTree.get(left, right);

      if(middle instanceof FingerTree) {
        final FingerTree<Node<N, E>, E> tree = (FingerTree<Node<N, E>, E>) middle;
        return DeepTree.get(left, tree, right);
      }

      final BufferNode<Node<N, E>, E> buffer = (BufferNode<Node<N, E>, E>) middle;
      return DeepTree.get(left, buffer.freeze(), right);
    }

    /**
     * Returns the node at the given position in this node's ring buffer.
     * @param pos position
     * @return node at that position
     */
    Node<N, E> get(final int pos) {
      return nodes[(((midPos + pos) % CAP) + CAP) % CAP];
    }

    /**
     * Returns the middle tree as a buffer node.
     * @return middle buffer node (can be {@code null})
     */
    private BufferNode<Node<N, E>, E> midBuffer() {
      if(middle == null) return null;
      if(middle instanceof BufferNode) return (BufferNode<Node<N, E>, E>) middle;
      final BufferNode<Node<N, E>, E> mid = new BufferNode<>((FingerTree<Node<N, E>, E>) middle);
      middle = mid;
      return mid;
    }

    /**
     * Copies the elements in the given range from the ring buffer into an array.
     * @param start start of the range
     * @param len length of the range
     * @return array containing all nodes in the range
     */
    private Node<N, E>[] copy(final int start, final int len) {
      final Node<N, E>[] out = new Node[len];
      copyInto(start, out, 0, len);
      return out;
    }

    /**
     * Copies the nodes in the given range of the ring buffer into the given array.
     * @param start start position of the range in the ring buffer
     * @param arr output array
     * @param pos start position in the output array
     * @param len length of the range
     */
    private void copyInto(final int start, final Node<N, E>[] arr, final int pos, final int len) {
      final int p = ((start % CAP) + CAP) % CAP, k = CAP - p;
      if(len <= k) {
        Array.copy(nodes, p, len, arr, pos);
      } else {
        Array.copy(nodes, p, k, arr, pos);
        Array.copyFromStart(nodes, len - k, arr, pos + k);
      }
    }
  }

  /**
   * Iterator over the elements in this builder.
   * @param <E> element type
   */
  private static class BufferNodeIterator<E> implements Iterator<E> {
    /** Stack of buffer nodes. */
    private BufferNode<?, E>[] stack = new BufferNode[8];
    /** Stack of position inside the buffer nodes. */
    private int[] poss = new int[8];
    /** Stack top. */
    private int top;
    /** Iterator over the current tree node. */
    private Iterator<E> sub;

    /**
     * Constructor.
     * @param root buffer node
     */
    BufferNodeIterator(final BufferNode<E, E> root) {
      stack[0] = root;
      final int pos = -root.inLeft;
      poss[0] = pos;
      sub = new FingerTreeIterator<>(root.get(pos), 0);
    }

    @Override
    public boolean hasNext() {
      return sub != null;
    }

    @Override
    public E next() {
      final E out = sub.next();
      if(sub.hasNext()) return out;

      // sub-iterator empty
      sub = null;
      final BufferNode<?, E> buffer = stack[top];
      poss[top]++;

      if(poss[top] < 0) {
        sub = new FingerTreeIterator<>(buffer.get(poss[top]), 0);
        return out;
      }

      if(poss[top] == 0) {
        final Object mid = buffer.middle;
        if(mid != null) {
          if(mid instanceof FingerTree) {
            sub = ((FingerTree<?, E>) mid).iterator();
          } else {
            final BufferNode<?, E> buff = (BufferNode<?, E>) mid;
            if(++top == stack.length) {
              stack = Arrays.copyOf(stack, 2 * top);
              poss = Arrays.copyOf(poss, 2 * top);
            }
            stack[top] = buff;
            poss[top] = -buff.inLeft;
            sub = new FingerTreeIterator<>(buff.get(poss[top]), 0);
          }
          return out;
        }
        poss[top]++;
      }

      if(poss[top] <= buffer.inRight) {
        sub = new FingerTreeIterator<>(buffer.get(poss[top] - 1), 0);
        return out;
      }

      stack[top] = null;
      if(--top >= 0) {
        sub = new FingerTreeIterator<>(stack[top].get(0), 0);
        poss[top]++;
      }

      return out;
    }

    @Override
    public void remove() {
      throw Util.notExpected();
    }
  }
}
