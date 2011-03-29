package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.ItemCache;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ItemSeq extends Seq {
  /** Item array. */
  private final Item[] item;
  /** Sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param it items
   * @param s size
   */
  protected ItemSeq(final Item[] it, final int s) {
    super(s);
    item = it;
  }

  @Override
  public Object toJava() {
    final Object[] obj = new Object[(int) size];
    for(int s = 0; s != size; ++s) obj[s] = item[s].toJava();
    return obj;
  }

  @Override
  public ItemCache iter() {
    return new ItemCache(item, (int) size);
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!item[0].node()) CONDTYPE.thrw(ii, this);
    return item[0];
  }

  @Override
  public SeqType type() {
    if(seq == null) {
      Type t = item[0].type;
      for(int s = 1; s != size && t != AtomType.ITEM; ++s) {
        if(t != item[s].type) t = AtomType.ITEM;
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
    final ItemSeq is = (ItemSeq) cmp;
    return item == is.item && size == is.size;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(Util.name(this)), SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); ++v) item[v].plan(ser);
    ser.closeElement();
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    int h = 0;
    for(int v = 0; v != Math.min(size, 5); ++v) h += item[v].hash(ii);
    return h;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAR1);
    for(int i = 0; i < size; ++i) {
      sb.append((i != 0 ? SEP : "") + item[i]);
      if(sb.length() > 32 && i + 1 != size) {
        sb.append(SEP + DOTS);
        break;
      }
    }
    return sb.append(PAR2).toString();
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    System.arraycopy(item, 0, arr, start, (int) size);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return item[(int) pos];
  }

  @Override
  public ItemCache cache() {
    return iter();
  }
}
