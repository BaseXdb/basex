package org.basex.query.value.array;

import org.basex.core.jobs.*;
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
  /** Interruptible job. */
  private final Job job;

  /** Builder, only instantiated if there are at least two items. */
  private ArrBuilder builder;
  /** Capacity ({@link Long#MIN_VALUE}: create no compact data structures). */
  private final long capacity;
  /** The first added value is cached. */
  private Value single;

  /**
   * Constructor.
   * @param job interruptible job
   */
  public ArrayBuilder(final Job job) {
    this(job, -1);
  }

  /**
   * Constructor.
   * @param job interruptible job
   * @param capacity initial capacity ({@link Long#MIN_VALUE}: create no compact data structures)
   */
  public ArrayBuilder(final Job job, final long capacity) {
    this.job = job;
    this.capacity = capacity;
  }

  /**
   * Appends a member to the array.
   * @param value value to append
   * @return reference to this builder for convenience
   */
  public ArrayBuilder add(final Value value) {
    if(builder == null) {
      job.checkStop();
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
   * Returns the built array.
   * @return array
   */
  public XQArray array() {
    return array(Types.ARRAY);
  }

  /**
   * Returns the built array and annotates it with the type of the specified expression.
   * @param expr expression that created the array
   * @return array
   */
  public XQArray array(final Expr expr) {
    return expr.seqType().type instanceof final ArrayType at  ? array(at) : array();
  }

  /**
   * Returns the built array and annotates it with the specified type.
   * @param type type (only considered if a new value is created)
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

  /** Item array builder. */
  final class ItemArrayBuilder extends ArrBuilder {
    /** Value builder. */
    private final ValueBuilder vb = new ValueBuilder(job, capacity);

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
