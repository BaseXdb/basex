package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.Iter;
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
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Str get(final byte[] v) {
    return v.length == 0 ? ZERO : new Str(v);
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
