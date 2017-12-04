package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
final class SubSeq extends Seq {
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
  protected Seq subSeq(final long offset, final long length, final QueryContext qc) {
    qc.checkStop();
    return new SubSeq(sub, start + offset, length);
  }

  @Override
  public Value insert(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  @Override
  public Value remove(final long pos, final QueryContext qc) {
    return copyRemove(pos, qc);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < size; i++) vb.addFront(sub.itemAt(start + i));
    return vb.value();
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    final int n = (int) Math.min(arr.length - index, size);
    for(int i = 0; i < n; i++) arr[index + i] = itemAt(i);
    return n;
  }

  @Override
  public Item itemAt(final long pos) {
    return sub.itemAt(start + pos);
  }

  @Override
  public boolean homogeneous() {
    return sub.homogeneous();
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item fst = itemAt(0);
    if(fst instanceof ANode) return fst;
    throw EBV_X.get(ii, this);
  }

  @Override
  public void materialize(final InputInfo ii) throws QueryException {
    for(long i = 0; i < size; i++) itemAt(i).materialize(ii);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < size; i++) vb.add(itemAt(i).atomValue(qc, ii));
    return vb.value();
  }

  @Override
  public long atomSize() {
    long s = 0;
    for(int i = 0; i < size; i++) s += itemAt(i).atomSize();
    return s;
  }
}
