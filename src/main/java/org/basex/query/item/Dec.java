package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Decimal item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Dec extends Item {
  /** Zero value. */
  private static final Dec ZERO = new Dec(BigDecimal.ZERO);
  /** Decimal value. */
  private final BigDecimal val;

  /**
   * Constructor.
   * @param t string representation
   */
  public Dec(final byte[] t) {
    super(Type.DEC);
    val = new BigDecimal(Token.string(trim(t)));
  }

  /**
   * Constructor.
   * @param d decimal value
   * @param t string representation
   */
  public Dec(final BigDecimal d, final Type t) {
    super(t);
    val = d;
  }

  /**
   * Constructor.
   * @param d decimal value
   */
  private Dec(final BigDecimal d) {
    super(Type.DEC);
    val = d;
  }

  /**
   * Constructor.
   * @param d big decimal value
   * @return value
   */
  public static Dec get(final BigDecimal d) {
    return d.signum() == 0 ? ZERO : new Dec(d);
  }

  /**
   * Constructor.
   * @param d big decimal value
   * @return value
   */
  public static Dec get(final double d) {
    return get(BigDecimal.valueOf(d));
  }

  @Override
  public byte[] atom() {
    return chopNumber(token(val.toPlainString()));
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return val.signum() != 0;
  }

  @Override
  public long itr(final InputInfo ii) {
    return val.longValue();
  }

  @Override
  public float flt(final InputInfo ii) {
    return val.floatValue();
  }

  @Override
  public double dbl(final InputInfo ii) {
    return val.doubleValue();
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return val;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return it.type == Type.DBL || it.type == Type.FLT ? it.eq(ii, this) :
      val.compareTo(it.dec(ii)) == 0;
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    final double d = it.dbl(ii);
    return d == 1 / 0.0 ? -1 : d == -1 / 0.0 ? 1 :
      d != d ? UNDEF : val.compareTo(it.dec(ii));
  }

  @Override
  public Object toJava() {
    switch(type) {
      case ULN:
        return new BigInteger(val.toString());
      default:
        return val;
    }
  }

  @Override
  public int hashCode() {
    return val.intValue();
  }

  /**
   * Converts the given double into a decimal value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  static BigDecimal parse(final double val, final InputInfo ii)
      throws QueryException {
    if(val != val || val == 1 / 0d || val == -1 / 0d)
      Err.or(ii, INVALUE, Type.DEC, val);
    return BigDecimal.valueOf(val);
  }

  /**
   * Converts the given token into a decimal value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  static BigDecimal parse(final byte[] val, final InputInfo ii)
      throws QueryException {

    if(contains(val, 'e') || contains(val, 'E'))
      Err.or(ii, FUNCAST, Type.DEC, val);

    try {
      return new BigDecimal(Token.string(val).trim());
    } catch(final NumberFormatException ex) {
      ZERO.castErr(val, ii);
      return null;
    }
  }
}
