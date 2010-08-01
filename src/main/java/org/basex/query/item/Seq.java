package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Item sequence.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Seq extends Item {
  /** Empty sequence. */
  public static final Seq EMPTY = new Seq() {
    @Override
    public Iter iter() { return Iter.EMPTY; }
    @Override
    public Item atomic(final QueryContext ctx) { return null; }
    @Override
    public Item ebv(final QueryContext ctx) { return Bln.FALSE; }
    @Override
    public Item test(final QueryContext ctx) { return null; }
    @Override
    public SeqType returned(final QueryContext ctx) { return SeqType.ITEM_Z; }
  };

  /** Item array. */
  public final Item[] val;
  /** Number of entries. */
  private final int size;

  /**
   * Constructor.
   */
  protected Seq() {
    super(Type.EMP);
    val = null;
    size = 0;
  }

  /**
   * Returns a sequence for the specified items.
   * @param v value
   * @param s size
   * @return resulting item or sequence
   */
  public static final Item get(final Item[] v, final int s) {
    return s == 0 ? EMPTY : s == 1 ? v[0] : new Seq(v, s);
  }

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

  @Override
  public final boolean item() {
    return false;
  }

  @Override
  public final long size(final QueryContext ctx) {
    return size;
  }

  @Override
  public final Object toJava() {
    final Object[] obj = new Object[size];
    for(int s = 0; s < size; s++) obj[s] = val[s].toJava();
    return obj;
  }

  @Override
  public Iter iter() {
    return new SeqIter(val, size);
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    Err.or(XPSEQ, this);
    return null;
  }

  @Override
  public Item ebv(final QueryContext ctx) throws QueryException {
    if(!val[0].node()) Err.or(CONDTYPE, this);
    return val[0];
  }

  @Override
  public Item test(final QueryContext ctx) throws QueryException {
    return ebv(ctx);
  }

  @Override
  public final boolean eq(final Item it) throws QueryException {
    castErr(it);
    return false;
  }

  @Override
  public final int diff(final ParseExpr e, final Item it) {
    Main.notexpected();
    return 0;
  }

  @Override
  public final void serialize(final Serializer ser) throws IOException {
    for(int i = 0; i < size; i++) {
      ser.openResult();
      val[i].serialize(ser);
      ser.closeResult();
    }
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    Type t = size > 128 ? Type.ITEM : val[0].type;
    for(int s = 1; s < size && t != Type.ITEM; s++) {
      if(t != val[s].type) t = Type.ITEM;
    }
    return new SeqType(t, SeqType.Occ.OM);
  }

  @Override
  public final boolean duplicates(final QueryContext ctx) {
    return size != 0;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(Token.token(Type.SEQ.name), SIZE, Token.token(size));
    for(int v = 0; v != Math.min(size, 5); v++) val[v].plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(int v = 0; v != size; v++) {
      sb.append((v != 0 ? ", " : "") + val[v]);
      if(sb.length() > 32 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
