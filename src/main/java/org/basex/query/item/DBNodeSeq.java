package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * {@link DBNode} Sequence, containing at least two nodes and no duplicates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DBNodeSeq extends Seq {
  /** Item array. */
  private final DBNode[] val;

  /**
   * Constructor.
   * @param v value
   * @param s size
   */
  public DBNodeSeq(final DBNode[] v, final int s) {
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

  /**
   * Returns a node iterator.
   * @return iterator
   */
  public NodIter nodIter() {
    return new NodIter(val, (int) size);
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return val[0];
  }

  @Override
  public SeqType type() {
    return SeqType.NOD_OM;
  }

  @Override
  public boolean duplicates() {
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof DBNodeSeq)) return false;
    final DBNodeSeq i = (DBNodeSeq) cmp;
    return val == i.val && size == i.size;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Type.SEQ.nam, SIZE, Token.token(size));
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
