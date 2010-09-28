package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.query.iter.RangeIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Range sequence, containing at least two integers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    for(int s = 0; s < size; ++s) obj[s] = Itr.get(start + s);
    return obj;
  }

  @Override
  public Iter iter() {
    return new RangeIter(start, start + size - 1);
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return item(ctx, ii);
  }

  @Override
  public SeqType type() {
    return SeqType.ITR_OM;
  }

  @Override
  public boolean duplicates() {
    return false;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, MIN, Token.token(start),
        MAX, Token.token(start + size - 1));
  }

  @Override
  public String toString() {
    return PAR1 + start + DOTS + (start + size - 1) + PAR2;
  }
}
