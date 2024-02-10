package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team 2005-24, BSD License
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
   */
  SubSeq(final Seq sub, final long start, final long length) {
    super(length, sub.type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    return new SubSeq(sub, start + pos, length);
  }

  @Override
  public Value insertBefore(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return copyRemove(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < size; i++) vb.addFront(itemAt(i));
    return vb.value(this);
  }

  @Override
  public Item itemAt(final long pos) {
    return sub.itemAt(start + pos);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(long i = 0; i < size; i++) itemAt(i).cache(lazy, ii);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < size; i++) vb.add(itemAt(i).atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }
}
