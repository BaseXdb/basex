package org.basex.query.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence of {@link Int Integers}, containing at least two of them.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class IntSeq extends Seq {
  /** Values. */
  private final long[] vals;

  /**
   * Constructor.
   * @param ints integers
   * @param t int type
   */
  private IntSeq(final long[] ints, final Type t) {
    super(ints.length, t);
    vals = ints;
  }

  @Override
  public boolean homogenous() {
    return true;
  }

  @Override
  public Item itemAt(final long pos) {
    return Int.get(vals[(int) pos], type);
  }

  @Override
  public long[] toJava() {
    return vals.clone();
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    final int w = Math.min(vals.length, arr.length - start);
    for(int i = 0; i < w; i++) arr[start + i] = itemAt(i);
    return w;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, this);
  }

  @Override
  public SeqType type() {
    return SeqType.get(type, Occ.ONE_MORE);
  }

  /**
   * Creates a sequence with the specified integers.
   * @param val integers
   * @param type type
   * @return value
   */
  public static Value get(final long[] val, final Type type) {
    return val.length == 0 ? Empty.SEQ : val.length == 1 ?
        Int.get(val[0], type) : new IntSeq(val, type);
  }

  /**
   * Creates a sequence with the integers in the specified expressions.
   * @param expr expressions
   * @param size size of resulting sequence
   * @param type type
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Expr[] expr, final long size, final Type type)
      throws QueryException {

    final long[] tmp = new long[(int) size];
    int t = 0;
    for(final Expr e : expr) {
      // speed up construction for items and integer sequences
      if(e instanceof Item) {
        tmp[t++] = ((Item) e).itr(null);
      } else if(e instanceof IntSeq) {
        final IntSeq val = (IntSeq) e;
        final long vs = val.size();
        for(int v = 0; v < vs; v++) tmp[t++] = val.vals[v];
      } else {
        final Value val = (Value) e;
        final long vs = val.size();
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).itr(null);
      }
    }
    return get(tmp, type);
  }
}
