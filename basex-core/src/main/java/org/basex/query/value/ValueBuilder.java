package org.basex.query.value;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;

/**
 * A builder for efficiently creating a {@link Value} by prepending and appending
 * {@link Item}s and {@link Value}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ValueBuilder {
  /** The first added value is cached. */
  private Value firstValue;
  /** Underlying sequence builder, only instantiated if there are at least two items. */
  private TreeSeqBuilder builder;

  /**
   * Concatenates two values.
   * @param v1 first value to concatenate
   * @param v2 second value to concatenate
   * @return value which contains all items of {@code v1} followed by all items of {@code v2}
   */
  public static Value concat(final Value v1, final Value v2) {
    final long s1 = v1.size();
    if(s1 == 0) return v2;
    final long s2 = v2.size();
    if(s2 == 0) return v1;
    if(s1 > 1) return ((Seq) v1).insertBefore(s1, v2);
    if(s2 > 1) return ((Seq) v2).insert(0, (Item) v1);
    return TreeSeqBuilder.value(new Item[] { (Item) v1, (Item) v2 }, 2, null);
  }

  /**
   * Returns a {@link Value} representation of the given items.
   * @param items array containing the items
   * @param n number of items
   * @param type item type of the resulting value (not checked), may be {@code null}
   * @return the value
   */
  public static Value value(final Item[] items, final int n, final Type type) {
    return n == 0 ? Empty.SEQ : n == 1 ? items[0] : TreeSeqBuilder.value(items, n, type);
  }

  /**
   * Adds an item to the front of the built value.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder addFront(final Item item) {
    final TreeSeqBuilder tree = builder;
    if(tree != null) {
      tree.addFront(item);
    } else {
      final Value first = firstValue;
      if(first != null) {
        builder = new TreeSeqBuilder().add(first).addFront(item);
        firstValue = null;
      } else {
        firstValue = item;
      }
    }
    return this;
  }

  /**
   * Appends a value to the end of the built value.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Value value) {
    if(value.isEmpty()) return this;

    final TreeSeqBuilder tree = builder;
    if(tree != null) {
      tree.add(value);
    } else {
      final Value first = firstValue;
      if(first != null) {
        builder = new TreeSeqBuilder().add(first).add(value);
        firstValue = null;
      } else {
        firstValue = value;
      }
    }
    return this;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder.
   * @return contents of this builder
   */
  public Value value() {
    return value(null);
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the given item type.
   * @param type item type, may be {@code null}
   * @return contents of this builder
   */
  public Value value(final Type type) {
    final Value first = firstValue;
    if(first != null) return first;
    final TreeSeqBuilder tree = builder;
    return tree != null ? tree.seq(type) : Empty.SEQ;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    final Iterator<Item> iter = firstValue != null ? firstValue.iterator() :
      builder != null ? builder.iterator() : Collections.<Item>emptyIterator();
    if(iter.hasNext()) {
      sb.append(iter.next());
      while(iter.hasNext()) sb.append(", ").append(iter.next());
    }
    return sb.append(']').toString();
  }
}
