package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Sequence, containing at least two items.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ItemSeq extends Seq {
  /** Item array. */
  private final Item[] val;
  /** Sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param v value
   * @param s size
   */
  protected ItemSeq(final Item[] v, final int s) {
    super(s);
    val = v;
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s != size; ++s) obj[s] = val[s].toJava();
    return obj;
  }

  @Override
  public ValueIter iter() {
    return new ItemIter(val, (int) size);
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!val[0].node()) CONDTYPE.thrw(ii, this);
    return val[0];
  }

  @Override
  public SeqType type() {
    if(seq == null) {
      Type t = val[0].type;
      for(int s = 1; s != size && t != Type.ITEM; ++s) {
        if(t != val[s].type) t = Type.ITEM;
      }
      seq = SeqType.get(t, SeqType.Occ.OM);
    }
    return seq;
  }

  @Override
  public boolean duplicates() {
    return true;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof ItemSeq)) return false;
    final ItemSeq i = (ItemSeq) cmp;
    return val == i.val && size == i.size;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v) val[v].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAR1);
    for(int v = 0; v != size; ++v) {
      sb.append((v != 0 ? SEP : "") + val[v]);
      if(sb.length() > 32 && v + 1 != size) {
        sb.append(SEP + DOTS);
        break;
      }
    }
    return sb.append(PAR2).toString();
  }
}
