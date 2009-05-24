package org.basex.query.item;

import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Str extends Item {
  /** String data. */
  public static final Str ZERO = new Str(Token.EMPTY);
  /** String data. */
  protected byte[] val;
  /** Direct parser creation (needed for QName types). */
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

  @Override
  public final byte[] str() {
    return val;
  }

  @Override
  public boolean bool() {
    return str().length != 0;
  }

  @Override
  public long itr() throws QueryException {
    return Itr.parse(val);
  }

  @Override
  public final float flt() throws QueryException {
    return Flt.parse(val);
  }

  @Override
  public final double dbl() throws QueryException {
    return Dbl.parse(val);
  }

  @Override
  public BigDecimal dec() throws QueryException {
    return Dec.parse(str());
  }
  
  @Override
  @SuppressWarnings("unused")
  public boolean eq(final Item it) throws QueryException {
    return Token.eq(val, it.str());
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final Item it) throws QueryException {
    return Token.diff(val, it.str());
  }

  @Override
  public String toString() {
    return "\"" + Token.string(val) + "\"";
  }
}
