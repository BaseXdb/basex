package org.basex.query.value.seq;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class SubSeq extends Seq {
  /** Underlying sequence. */
  private final Seq sub;
  /** Starting index in {@link #sub}. */
  private final long start;

  /**
   * Factory method for subsequences.
   * @param val underlying value
   * @param from starting index
   * @param len length of the subsequence
   * @return the resulting value
   */
  public static Value get(final Value val, final long from, final long len) {
    final long vLen = val.size(), n = Math.min(vLen - from, len);
    if(n == vLen) return val;
    if(n <= 0) return Empty.SEQ;
    if(n == 1) return val.itemAt(from);
    if(val instanceof SubSeq) {
      final SubSeq sSeq = (SubSeq) val;
      return new SubSeq(sSeq.sub, sSeq.start + from, n);
    }
    // cast is safe because n >= 2
    return new SubSeq((Seq) val, from, n);
  }

  /**
   * Constructor.
   * @param sub underlying sequence
   * @param start starting index
   * @param len length of the subsequence
   */
  private SubSeq(final Seq sub, final long start, final long len) {
    super(len, sub.type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  public Value reverse() {
    final int n = (int) size;
    final Item[] items = new Item[n];
    for(int i = 0; i < n; i++) items[n - 1 - i] = sub.itemAt(start + i);
    return Seq.get(items, n);
  }

  @Override
  public int writeTo(final Item[] arr, final int index) {
    final int n = (int) Math.min(arr.length - index, size);
    for(int i = 0; i < n; i++) arr[index + i] = sub.itemAt(start + i);
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
    throw EBV.get(ii, this);
  }

  @Override
  public SeqType seqType() {
    return sub.seqType();
  }
}
