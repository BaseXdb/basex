package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Sequence, containing at least two items.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Seq extends Value {
  /** Item array. */
  public final Item[] val;
  /** Number of entries. */
  private final int size;

  /**
   * Constructor.
   * @param v value
   * @param s size
   */
  private Seq(final Item[] v, final int s) {
    super(Type.SEQ);
    val = v;
    size = s;
  }

  /**
   * Returns a sequence for the specified items.
   * @param v value
   * @param s size
   * @return resulting item or sequence
   */
  public static Value get(final Item[] v, final int s) {
    return s == 0 ? Empty.SEQ : s == 1 ? v[0] : new Seq(v, s);
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[size];
    for(int s = 0; s != size; ++s) obj[s] = val[s].toJava();
    return obj;
  }

  @Override
  public Iter iter() {
    return new ItemIter(val, size);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    Err.or(ii, XPSEQ, this);
    return null;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!val[0].node()) Err.or(ii, CONDTYPE, this);
    return val[0];
  }

  @Override
  public Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return ebv(ctx, ii);
  }

  @Override
  public SeqType type() {
    Type t = size > 128 ? Type.ITEM : val[0].type;
    for(int s = 1; s != size && t != Type.ITEM; ++s) {
      if(t != val[s].type) t = Type.ITEM;
    }
    return new SeqType(t, SeqType.Occ.OM);
  }

  @Override
  public boolean duplicates() {
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v) val[v].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(int v = 0; v != size; ++v) {
      sb.append((v != 0 ? ", " : "") + val[v]);
      if(sb.length() > 32 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
