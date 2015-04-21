package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team 2005-15, BSD License
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
   * @param len length of the subsequence
   */
  SubSeq(final Seq sub, final long start, final long len) {
    super(len, sub.type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  public Value subSeq(final long off, final long len) {
    return len == 0   ? Empty.SEQ
         : len == 1   ? sub.itemAt(start + off)
         : len < size ? new SubSeq(sub, start + off, len)
                      : this;
  }

  @Override
  public Value insert(final long pos, final Item item) {
    return copyInsert(pos, item);
  }

  @Override
  public Value remove(final long pos) {
    return copyRemove(pos);
  }

  @Override
  public Value reverse() {
    final ValueBuilder vb = new ValueBuilder();
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
  public SeqType seqType() {
    return sub.seqType();
  }

  @Override
  public Value materialize(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < size; i++) vb.add(itemAt(i).materialize(ii));
    return vb.value();
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < size; i++) vb.add(itemAt(i).atomValue(ii));
    return vb.value();
  }

  @Override
  public long atomSize() {
    long s = 0;
    for(int i = 0; i < size; i++) s += itemAt(i).atomSize();
    return s;
  }
}
