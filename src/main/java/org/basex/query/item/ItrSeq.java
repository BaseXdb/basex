package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Sequence of {@link Itr Integers}, containing at least two of them.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class ItrSeq extends Seq {
  /** Values. */
  final long[] vals;

  /**
   * Constructor.
   * @param ints integers
   * @param t int type
   */
  private ItrSeq(final long[] ints, final Type t) {
    super(ints.length, t);
    vals = ints;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    long hash = 0;
    for(final long i : vals) hash = 31 * hash + i;
    return (int) hash;
  }

  @Override
  public boolean homogenous() {
    return true;
  }

  @Override
  public Item itemAt(final long pos) {
    return Itr.get(vals[(int) pos], type);
  }

  @Override
  public ValueIter iter() {
    return new ValueIter() {
      /** Position of this iterator. */
      int pos;
      @Override
      public Value finish() { return ItrSeq.this; }
      @Override
      public Item get(final long i) { return itemAt(i); }
      @Override
      public Item next() { return pos < size() ? itemAt(pos++) : null; }
      @Override
      public boolean reset() { pos = 0; return true; }
      @Override
      public long size() { return vals.length; }
    };
  }

  @Override
  public long[] toJava() {
    return vals.clone();
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    int w = Math.min(vals.length, arr.length - start);
    for(int i = 0; i < w; i++) arr[start + i] = itemAt(i);
    return w;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    throw CONDTYPE.thrw(ii, this);
  }

  @Override
  public SeqType type() {
    return SeqType.get(type, Occ.OM);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(Util.name(this)), SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v)
      ser.emptyElement(ITM, VAL, Token.token(vals[v]), TYP, type.nam());
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(QueryTokens.PAR1);
    for(int i = 0; i < vals.length; i++)
      tb.add(i > 0 ? ", " : "").addLong(vals[i]);
    return tb.add(QueryTokens.PAR2).toString();
  }

  /**
   * Creates a value containing the given longs in the given order.
   * @param val longs
   * @param t type
   * @return value
   */
  public static Value get(final long[] val, final Type t) {
    return val.length == 0 ? Empty.SEQ : val.length == 1
        ? Itr.get(val[0], t) : new ItrSeq(val, t);
  }

  /**
   * Creates a value containing the given integers in the given order.
   * @param val integers
   * @param t type
   * @return value
   */
  public static Value get(final int[] val, final Type t) {
    if(val.length < 2) return val.length == 0 ? Empty.SEQ : Itr.get(val[0], t);
    final long[] nv = new long[val.length];
    for(int i = 0; i < nv.length; i++) nv[i] = val[i];
    return new ItrSeq(nv, t);
  }

  /**
   * Creates a value containing the given integers in the given order.
   * @param val integers
   * @param t type
   * @return value
   */
  public static Value get(final byte[] val, final Type t) {
    if(val.length < 2) return val.length == 0 ? Empty.SEQ : Itr.get(val[0], t);
    final long[] nv = new long[val.length];
    for(int i = 0; i < nv.length; i++) nv[i] = val[i];
    return new ItrSeq(nv, t);
  }

}
