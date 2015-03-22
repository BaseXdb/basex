package org.basex.query.util.fingertree;

import java.util.*;

/**
 * A node of a FingerTree.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public abstract class FingerTree<N, E> implements Iterable<E> {
  /**
   * Returns the empty finger tree.
   * @param <E> element type
   * @return empty finger tree
   */
  @SuppressWarnings("unchecked")
  public static final <E> FingerTree<E, E> empty() {
    return (FingerTree<E, E>) Empty.INSTANCE;
  }

  /**
   * Creates a singleton finger tree containing the given leaf node.
   * @param <E> element type
   * @param leaf the contained leaf
   * @return the singleton finger tree
   */
  public static final <E> FingerTree<E, E> singleton(final Node<E, E> leaf) {
    return new Single<>(leaf);
  }

  /**
   * Checks if this node is empty.
   * @return {@code true} if the node is empty, {@code false} otherwise
   */
  public final boolean isEmpty() {
    return this == Empty.INSTANCE;
  }

  /**
   * Returns the element at the given position in this tree.
   * @param index index of the element
   * @return the element
   */
  public final E get(final long index) {
    // iterate down the spine
    long pos = index;
    FingerTree<?, E> curr = this;
    int level = 0;
    final Node<?, E> digit;
    while(true) {
      if(curr instanceof Single) {
        // we unpack the contained node one level
        digit = ((Single<?, E>) curr).elem;
        break;
      }

      final Deep<?, E> deep = (Deep<?, E>) curr;
      // check if index is in left digit
      if(pos < deep.leftSize) {
        Node<?, E> nd = null;
        for(int i = 0; i < deep.left.length; i++) {
          nd = deep.left[i];
          final long s = nd.size();
          if(pos < s) break;
          pos -= s;
        }
        digit = nd;
        break;
      }

      // check if index is in middle tree
      pos -= deep.leftSize;
      final long mSize = deep.middle.size();
      if(pos >= mSize) {
        // index is in right digit
        pos -= mSize;
        Node<?, E> nd = null;
        for(int i = 0; i < deep.right.length; i++) {
          nd = deep.right[i];
          final long s = nd.size();
          if(pos < s) break;
          pos -= s;
        }
        digit = nd;
        break;
      }

      // recurse into the middle tree
      curr = deep.middle;
      level++;
    }

    Node<?, ?> nd = digit;
    for(; level > 0; level--) {
      if(nd instanceof InnerNode2) {
        final InnerNode2<?, ?> deep = (InnerNode2<?, ?>) nd;
        if(pos < deep.l) {
          nd = deep.child0;
        } else {
          nd = deep.child1;
          pos -= deep.l;
        }
      } else {
        final InnerNode3<?, ?> deep = (InnerNode3<?, ?>) nd;
        if(pos < deep.l) {
          nd = deep.child0;
        } else if(pos < deep.m) {
          nd = deep.child1;
          pos -= deep.l;
        } else {
          nd = deep.child2;
          pos -= deep.m;
        }
      }
    }

    @SuppressWarnings("unchecked")
    final E res = ((Node<E, E>) nd).getSub((int) pos);
    return res;
  }

  /**
   * The size of this tree.
   * @return number of elements in this tree
   */
  public abstract long size();

  /**
   * Adds an element to the front of this tree.
   * @param fst new first element
   * @return updated tree
   */
  public abstract FingerTree<N, E> cons(final Node<N, E> fst);

  /**
   * Adds an element to the end of this tree.
   * @param lst new last element
   * @return updated tree
   */
  public abstract FingerTree<N, E> snoc(final Node<N, E> lst);

  /**
   * Returns the first element of this tree.
   * @return first element
   * @throws NoSuchElementException if the tree is empty
   */
  public abstract Node<N, E> head();

  /**
   * Returns this tree removing the first element.
   * @return updated tree
   */
  public abstract FingerTree<N, E> tail();

  /**
   * Returns the last element of this tree.
   * @return last element
   * @throws NoSuchElementException if the tree is empty
   */
  public abstract Node<N, E> last();

  /**
   * Returns this tree removing the last element.
   * @return updated tree
   */
  public abstract FingerTree<N, E> init();

  /**
   * Concatenates this finger tree with the given one.
   * @param mid nodes between the two trees
   * @param other the other tree
   * @return concatenation of both trees
   */
  public abstract FingerTree<N, E> concat(final Node<N, E>[] mid, final FingerTree<N, E> other);

  /**
   * Creates a reversed version of this tree.
   * @return reversed tree
   */
  public abstract FingerTree<N, E> reverse();

  /**
   * Inserts the given value at the given position into this tree.
   * @param pos position to insert at
   * @param val value to insert
   * @return resulting tree
   */
  public abstract FingerTree<N, E> insert(final long pos, final E val);

  /**
   * Removes an element from this tree.
   * @param pos position of the element to remove
   * @return resulting (potentially partial) tree
   * @throws AssertionError if this tree is empty
   */
  public abstract TreeSlice<N, E> remove(final long pos);

  /**
   * Extracts a slice from this tree containing the {@code len} elements starting with that at
   * position {@code pos}.
   * @param pos position of the first element
   * @param len number of elements
   * @return resulting slice
   */
  public abstract TreeSlice<N, E> slice(final long pos, final long len);

  /**
   * Replaces the first node in this tree.
   * @param head new first node
   * @return resulting tree
   */
  public abstract FingerTree<N, E> replaceHead(final Node<N, E> head);

  /**
   * Replaces the last node in this tree.
   * @param last new last node
   * @return resulting tree
   */
  public abstract FingerTree<N, E> replaceLast(final Node<N, E> last);

  /**
   * Recursively constructs a finger tree from an array of nodes.
   * @param <N> node type
   * @param <E> element type
   * @param nodes node array
   * @param n number of nodes in the array
   * @param size size of all nodes combined
   * @return constructed tree
   */
  public static <N, E> FingerTree<N, E> buildTree(final Node<N, E>[] nodes, final int n,
      final long size) {
    if(n == 1) return new Single<>(nodes[0]);
    if(n < 7) {
      final int mid = n / 2;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] left = new Node[mid], right = new Node[n - mid];
      System.arraycopy(nodes, 0, left, 0, mid);
      System.arraycopy(nodes, mid, right, 0, n - mid);
      return Deep.get(left, right, size);
    }

    final int k = n == 7 ? 2 : 3;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[k], right = new Node[k];
    System.arraycopy(nodes, 0, left, 0, k);
    System.arraycopy(nodes, n - k, right, 0, k);
    final long leftSize = Deep.size(left), rightSize = Deep.size(right);

    int remaining = n - 2 * k;
    int i = k, j = 0;
    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] outNodes = (Node<Node<N, E>, E>[]) nodes;
    while(remaining > 4 || remaining == 3) {
      outNodes[j++] = new InnerNode3<>(nodes[i++], nodes[i++], nodes[i++]);
      remaining -= 3;
    }

    while(remaining > 0) {
      outNodes[j++] = new InnerNode2<>(nodes[i++], nodes[i++]);
      remaining -= 2;
    }

    final FingerTree<Node<N, E>, E> middle = buildTree(outNodes, j, size - leftSize - rightSize);
    return new Deep<>(left, leftSize, middle, right, size);
  }

  /**
   * Adds all nodes in the given array to the given side of this tree.
   * @param nodes the nodes
   * @param left insertion direction, {@code true} adds to the left, {@code false} to the right
   * @return resulting tree
   */
  abstract FingerTree<N, E> addAll(final Node<N, E>[] nodes, final boolean left);

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    toString(sb, 0);
    return sb.toString();
  }

  /**
   * Recursive helper method for {@link #toString()}.
   * @param sb string builder
   * @param indent indentation depth
   */
  abstract void toString(final StringBuilder sb, final int indent);

  /**
   * Checks that this tree does not violate any invariants.
   * @return number of elements in this tree
   * @throws AssertionError if any invariant was violated
   */
  public abstract long checkInvariants();

  /**
   * Returns an array containing the number of elements stored at each level of the tree.
   * @param depth current depth
   * @return array of sizes
   */
  public abstract long[] sizes(int depth);

  /**
   * Creates a {@link ListIterator} over the elements in this tree.
   * @param reverse flag for starting at the back of the tree
   * @return the list iterator
   */
  public abstract ListIterator<E> listIterator(final boolean reverse);

  @Override
  public final ListIterator<E> iterator() {
    return listIterator(false);
  }
}
