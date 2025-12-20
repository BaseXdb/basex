package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * A builder for efficiently creating an {@link XQArray}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayBuilder {
  /** QueryContext (can be {@code null}). */
  private final QueryContext qc;

  /** Sequence builder, only instantiated if there are at least two members. */
  private ArrBuilder array;
  /** Capacity ({@link Integer#MIN_VALUE}: create no compact data structures). */
  private final long capacity;
  /** The first added value is cached. */
  private Value single;

  /**
   * Constructor.
   */
  public ArrayBuilder() {
    this(null);
  }

  /**
   * Constructor.
   * @param qc query context (can be {@code null}, required for interrupting running queries)
   */
  public ArrayBuilder(final QueryContext qc) {
    this(qc, -1);
  }

  /**
   * Constructor.
   * @param qc query context (can be {@code null}, required for interrupting running queries)
   * @param capacity initial capacity ({@link Integer#MIN_VALUE}: create no compact data structures)
   */
  public ArrayBuilder(final QueryContext qc, final long capacity) {
    this.qc = qc;
    this.capacity = qc != null ? capacity : Integer.MIN_VALUE;
  }

  /**
   * Appends a member to the array.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ArrayBuilder add(final Value value) {
    if(array == null) {
      final Value sngl = single;
      if(sngl == null) {
        single = value;
        return this;
      }
      single = null;
      array = capacity == Integer.MIN_VALUE || sngl.size() != 1 || value.size() != 1 ?
        new TreeArrayBuilder() : new ItemArrayBuilder(qc, capacity);
      add(sngl);
    }
    array = array.add(value);
    return this;
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder.
   * @return value
   */
  public XQArray array() {
    return array(Types.ARRAY);
  }

  /**
   * Returns a {@link Value} representation of the items currently stored in this builder
   * annotated with the given item type.
   * @param type type (only considered if new result value is created)
   * @return value
   */
  public XQArray array(final ArrayType type) {
    try {
      return array != null ? array.array(type) : single != null ? XQArray.get(single) :
        XQArray.empty();
    } finally {
      array = null;
      single = null;
    }
  }

  /**
   * Creates an {@link XQArray} containing the members of this builder.
   * @param expr expression that created the array (can be {@code null})
   * @return resulting array
   */
  public XQArray array(final Expr expr) {
    return expr != null ? array((ArrayType) expr.seqType().type) : array();
  }
}
