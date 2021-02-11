package org.basex.query.value;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for efficiently creating a {@link Value} by prepending and appending
 * {@link Item}s and {@link Value}s.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class ValueBuilder {
  /** QueryContext. */
  private final QueryContext qc;

  /** The first added value is cached. */
  private Value firstValue;
  /** Underlying sequence builder, only instantiated if there are at least two items. */
  private TreeSeqBuilder builder;

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   */
  public ValueBuilder(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Constructor with initial items.
   * @param qc query context (required for interrupting running queries)
   * @param item1 first item to append
   * @param item2 second item to append
   */
  public ValueBuilder(final QueryContext qc, final Item item1, final Item item2) {
    this(qc);
    builder = new TreeSeqBuilder().add(item1).add(item2);
  }

  /**
   * Concatenates two values.
   * @param value1 first value to concatenate
   * @param value2 second value to concatenate
   * @param qc query context
   * @return concatenated values
   */
  public static Value concat(final Value value1, final Value value2, final QueryContext qc) {
    // return existing values
    final long size1 = value1.size();
    if(size1 == 0) return value2;
    final long size2 = value2.size();
    if(size2 == 0) return value1;
    // prepend or append values
    if(size1 > 1) return ((Seq) value1).insertBefore(size1, value2, qc);
    if(size2 > 1) return ((Seq) value2).insert(0, (Item) value1, qc);
    // concatenate single items
    return TreeSeqBuilder.concat((Item) value1, (Item) value2);
  }

  /**
   * Adds an item to the front of the built value.
   * @param item item to add
   * @return reference to this builder for convenience
   */
  public ValueBuilder addFront(final Item item) {
    qc.checkStop();
    final TreeSeqBuilder tree = builder;
    if(tree != null) {
      tree.addFront(item);
    } else {
      final Value first = firstValue;
      if(first != null) {
        builder = new TreeSeqBuilder().add(first, qc).addFront(item);
        firstValue = null;
      } else {
        firstValue = item;
      }
    }
    return this;
  }

  /**
   * Appends an item to the built value.
   * @param item item to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Item item) {
    qc.checkStop();
    final TreeSeqBuilder tree = builder;
    if(tree != null) {
      tree.add(item);
    } else {
      final Value first = firstValue;
      if(first != null) {
        builder = new TreeSeqBuilder().add(first, qc).add(item);
        firstValue = null;
      } else {
        firstValue = item;
      }
    }
    return this;
  }

  /**
   * Appends a value to the built value.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ValueBuilder add(final Value value) {
    if(value.isEmpty()) {
      qc.checkStop();
      return this;
    }

    final TreeSeqBuilder tree = builder;
    if(tree != null) {
      tree.add(value, qc);
    } else {
      final Value first = firstValue;
      if(first != null) {
        builder = new TreeSeqBuilder().add(first, qc).add(value, qc);
        firstValue = null;
      } else {
        firstValue = value;
      }
    }
    return this;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder.
   * @return value
   */
  public Value value() {
    return value((Type) null);
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the given item type.
   * @param type type (can be {@code null}, only considered if new sequence is created)
   * @return value
   */
  public Value value(final Type type) {
    final Value first = firstValue;
    if(first != null) return first;
    final TreeSeqBuilder tree = builder;
    return tree != null ? tree.seq(type) : Empty.VALUE;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the type of the given expression.
   * @param expr expression that created the value (can be {@code null})
   * @return value
   */
  public Value value(final Expr expr) {
    return value(expr != null ? expr.seqType().type : null);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    final Iterator<Item> iter = firstValue != null ? firstValue.iterator() :
      builder != null ? builder.iterator() : Collections.emptyIterator();
    if(iter.hasNext()) {
      sb.append(iter.next());
      while(iter.hasNext()) sb.append(", ").append(iter.next());
    }
    return sb.append(']').toString();
  }
}
