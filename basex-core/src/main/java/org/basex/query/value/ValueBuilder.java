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
    final long l = v1.size();
    if(l == 0) return v2;
    final long r = v2.size();
    if(r == 0) return v1;
    if(l > 1) return ((Seq) v1).insertBefore(l, v2);
    if(r > 1) return ((Seq) v2).insert(0, (Item) v1);
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
    return TreeSeqBuilder.value(items, n, type);
  }

  /**
   * Adds an item to the front of the built value.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder addFront(final Item item) {
    if(builder != null) {
      builder.addFront(item);
    } else if(firstValue != null) {
      builder = new TreeSeqBuilder().add(firstValue).addFront(item);
      firstValue = null;
    } else {
      firstValue = item;
    }
    return this;
  }

  /**
   * Adds an item to the end of the built value.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Item item) {
    if(builder != null) {
      builder.add(item);
    } else if(firstValue != null) {
      builder = new TreeSeqBuilder().add(firstValue).add(item);
      firstValue = null;
    } else {
      firstValue = item;
    }
    return this;
  }

  /**
   * Adds a value to the front of the built value.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Value value) {
    if(value.isEmpty()) return this;

    if(builder != null) {
      builder.add(value);
    } else if(firstValue != null) {
      builder = new TreeSeqBuilder().add(firstValue).add(value);
      firstValue = null;
    } else {
      firstValue = value;
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
    return firstValue != null ? firstValue : builder != null ? builder.value(type) : Empty.SEQ;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
    Iterator<Item> iter = firstValue != null ? firstValue.iterator() :
          builder != null ? builder.iterator() : Collections.<Item>emptyIterator();
    if(iter.hasNext()) {
      sb.append(iter.next());
      while(iter.hasNext()) sb.append(", ").append(iter.next());
    }
    return sb.append(']').toString();
  }
}
