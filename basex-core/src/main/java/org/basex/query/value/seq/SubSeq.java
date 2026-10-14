package org.basex.query.value.seq;

import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class SubSeq extends Seq {
  /** Underlying sequence. */
  private final Seq sub;
  /** Starting index in {@link #sub}. */
  private final long start;

  /**
   * Constructor.
   * @param sub underlying sequence
   * @param start starting index
   * @param length length of the subsequence
   * @param type type
   */
  SubSeq(final Seq sub, final long start, final long length, final Type type) {
    super(length, type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  public Item itemAt(final long index) {
    return sub.itemAt(start + index);
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final Job job) {
    return new SubSeq(sub, start + pos, length, type);
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final boolean funcs, final InputInfo ii)
      throws QueryException {
    return sub instanceof NativeSeq || sub instanceof SingletonSeq ?
      sub.materialized(test, funcs, ii) : super.materialized(test, funcs, ii);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof final SubSeq ss ? start == ss.start &&
        size == ss.size && sub.equals(ss.sub) : super.equals(obj));
  }

  @Override
  public Value shrink(final QueryContext qc) throws QueryException {
    return rebuild(qc);
  }
}
