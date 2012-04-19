package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Range sequence, containing at least two integers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RangeSeq extends Seq {
  /** Start value. */
  private final long start;

  /**
   * Constructor.
   * @param s start value
   * @param sz size
   */
  public RangeSeq(final long s, final long sz) {
    super(sz);
    start = s;
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s < size; ++s) obj[s] = Int.get(start + s);
    return obj;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    throw CONDTYPE.thrw(ii, this);
  }

  @Override
  public SeqType type() {
    return SeqType.ITR_OM;
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public int writeTo(final Item[] arr, final int pos) {
    for(int i = 0; i < size; i++) arr[pos + i] = itemAt(i);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return Int.get(start + pos);
  }

  @Override
  public boolean homogenous() {
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(Token.token(Util.name(this)),
        MIN, Token.token(start), MAX, Token.token(start + size - 1));
  }

  @Override
  public String toString() {
    return PAR1 + start + ' ' + TO + ' ' + (start + size - 1) + PAR2;
  }
}
