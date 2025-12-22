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
  /** QueryContext. */
  private final QueryContext qc;

  /** Builder, only instantiated if there are at least two items. */
  private ArrBuilder builder;
  /** Capacity ({@link Long#MIN_VALUE}: create no compact data structures). */
  private final long capacity;
  /** The first added value is cached. */
  private Value single;

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   */
  public ArrayBuilder(final QueryContext qc) {
    this(qc, -1);
  }

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   * @param capacity initial capacity ({@link Long#MIN_VALUE}: create no compact data structures)
   */
  public ArrayBuilder(final QueryContext qc, final long capacity) {
    this.qc = qc;
    this.capacity = capacity;
  }

  /**
   * Appends a member to the array.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ArrayBuilder add(final Value value) {
    if(builder == null) {
      qc.checkStop();
      final Value sngl = single;
      if(sngl == null) {
        single = value;
        return this;
      }
      single = null;
      builder = capacity == Long.MIN_VALUE || sngl.size() != 1 || value.size() != 1 ?
        new TreeArrayBuilder() : new ItemArrayBuilder();
      add(sngl);
    }
    builder = builder.add(value);
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
      return builder != null ? builder.array(type) : single != null ? XQArray.get(single) :
        XQArray.empty();
    } finally {
      builder = null;
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

  /** Item array builder. */
  final class ItemArrayBuilder extends ArrBuilder {
    /** Value builder. */
    private final ValueBuilder vb = new ValueBuilder(qc, capacity);

    @Override
    public ArrBuilder add(final Value value) {
      if(value.size() == 1) {
        vb.add(value);
        return this;
      }
      final TreeArrayBuilder ab = new TreeArrayBuilder();
      for(final Value member : vb.value()) ab.add(member);
      return ab.add(value);
    }

    @Override
    public XQArray array(final ArrayType type) {
      return new ItemArray(vb.value(type.valueType().type));
    }
  }
}
