package org.basex.query.value.array;

import org.basex.core.jobs.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A builder for efficiently creating an {@link XQArray}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayBuilder {
  /** Interruptible job. */
  private final Job job;

  /** Builder, only instantiated if there are at least two items (can be {@code null}). */
  private ArrBuilder builder;
  /** Capacity ({@link Long#MIN_VALUE}: create no compact data structures). */
  private final long capacity;
  /** The first added value is cached (can be {@code null}). */
  private Value single;
  /** Union of all member types (can be {@code null}). */
  private SeqType memberType;

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
    job.checkStop();
    refine(value.seqType());
    if(builder == null) {
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
   * Appends all members of an array.
   * @param array array
   * @return reference to this builder for convenience
   */
  public ArrayBuilder addMembers(final XQArray array) {
    final ArrBuilder ab = builder;
    if(array instanceof final ItemArray ia && capacity != Long.MIN_VALUE &&
        (ab == null ? single == null || single.size() == 1 : ab instanceof ItemArrayBuilder)) {
      // single-item members: append the members as sequence
      job.checkStop();
      final ItemArrayBuilder iab = ab != null ? (ItemArrayBuilder) ab : new ItemArrayBuilder();
      if(single != null) iab.vb.add(single);
      single = null;
      // large sequences: switch to tree representation, which reuses the appended sequence
      if(ia.members.size() >= Array.INITIAL_CAPACITY) iab.vb.tree(1);
      iab.vb.add(ia.members);
      refine(((ArrayType) ia.type).memberType());
      builder = iab;
    } else if(array instanceof final BigArray big && !(ab instanceof ItemArrayBuilder)) {
      // tree array: reuse its middle tree
      job.checkStop();
      final TreeArrayBuilder tab = ab != null ? (TreeArrayBuilder) ab : new TreeArrayBuilder();
      if(single != null) tab.add(single);
      single = null;
      tab.add(big, job);
      refine(((ArrayType) big.type).memberType());
      builder = tab;
    } else {
      for(final Value member : array.members()) add(member);
    }
    return this;
  }

  /**
   * Refines the member type.
   * @param type type of the added member(s)
   */
  private void refine(final SeqType type) {
    if(memberType == null) memberType = type;
    else if(!type.eq(memberType)) memberType = memberType.union(type);
  }

  /**
   * Returns the built array.
   * @return array
   */
  public XQArray array() {
    return array(memberType != null ? ArrayType.get(memberType) : Types.ARRAY);
  }

  /**
   * Returns the built array and annotates it with the type of the specified expression.
   * @param expr expression that created the array
   * @return array
   */
  public XQArray array(final Expr expr) {
    return expr.seqType().type.toAttachable() instanceof final ArrayType at ? array(at) : array();
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
      memberType = null;
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
      return new ItemArray(vb.value(type.memberType().type));
    }
  }
}
