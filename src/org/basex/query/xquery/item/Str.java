package org.basex.query.xquery.item;

import java.math.BigDecimal;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FTIndex;
import org.basex.query.xquery.expr.FTIndexInfo;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.Step;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Str extends Item {
  /** String data. */
  public static final Str ZERO = new Str(Token.EMPTY);
  /** String data. */
  protected byte[] val;
  /** Direct parser creation (only needed for QName check). */
  public boolean direct;

  /**
   * Constructor.
   * @param v value
   */
  private Str(final byte[] v) {
    this(v, Type.STR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  protected Str(final byte[] v, final Type t) {
    super(t);
    val = v;
  }

  /**
   * Constructor.
   * @param v value
   * @param d direct flag
   */
  public Str(final byte[] v, final boolean d) {
    this(v);
    direct = d;
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Str get(final byte[] v) {
    return v.length == 0 ? ZERO : new Str(v);
  }

  /**
   * Returns an instance of this class.
   * @param v object (will be converted to token)
   * @return instance
   */
  public static Str get(final Object v) {
    return get(Token.token(v.toString()));
  }

  /**
   * Returns an iterator.
   * @param v value
   * @return item
   */
  public static Iter iter(final byte[] v) {
    return new Iter() {
      boolean more;
      @Override
      public Item next() { return (more ^= true) ? get(v) : null; }
      @Override
      public String toString() { return Token.string(v); }
    };
  }

  @Override
  public final byte[] str() {
    return val;
  }

  @Override
  public boolean bool() {
    return str().length != 0;
  }

  @Override
  public long itr() throws XQException {
    return Itr.parse(val);
  }

  @Override
  public final float flt() throws XQException {
    return Flt.parse(val);
  }

  @Override
  public final double dbl() throws XQException {
    return Dbl.parse(val);
  }

  @Override
  public BigDecimal dec() throws XQException {
    return Dec.parse(str());
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final FTIndexInfo ii, 
      final Step curr) throws XQException {
    ii.tn++;
    DNode dn = (DNode) ctx.coll(null).next();
    FTTokenizer fto = new FTTokenizer(val);
    int i = 0;
    while(fto.more()) {
      final int n = dn.data.nrIDs(fto);
      // final int n = ctx.item.data.nrIDs(fto.sb);
      if(n == 0) {
        ii.indexSize = 0;
        return Bln.FALSE;
      }
      i = Math.max(i, n);
     }
     ii.indexSize = i;
    return new FTIndex(val, ii.lt, ii.tn);
  }
  
  @Override
  @SuppressWarnings("unused")
  public boolean eq(final Item it) throws XQException {
    return Token.eq(val, it.str());
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final Item it) throws XQException {
    return Token.diff(val, it.str());
  }

  @Override
  public String toString() {
    return "\"" + Token.string(val) + "\"";
  }
}
