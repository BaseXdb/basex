package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.basex.query.QueryException;
import org.basex.query.util.Err;

/**
 * Decimal item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Dec extends Item {
  /** Zero value. */
  public static final Dec ZERO = new Dec(BigDecimal.ZERO);
  /** Decimal value. */
  private BigDecimal val;

  /**
   * Constructor.
   * @param t string representation
   */
  public Dec(final byte[] t) {
    super(Type.DEC);
    val = new BigDecimal(string(trim(t)));
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
  public byte[] str() {
    return chopNumber(token(val.toPlainString()));
  }

  @Override
  public boolean bool() {
    return val.signum() != 0;
  }

  @Override
  public long itr() {
    return val.longValue();
  }

  @Override
  public float flt() {
    return val.floatValue();
  }

  @Override
  public double dbl() {
    return val.doubleValue();
  }

  @Override
  public BigDecimal dec() {
    return val;
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    return it.type == Type.DBL || it.type == Type.FLT ? it.eq(this) :
      val.compareTo(it.dec()) == 0;
  }

  @Override
  public int diff(final Item it) throws QueryException {
    final double d = it.dbl();
    return d == 1 / 0.0 ? -1 : d == -1 / 0.0 ? 1 :
      d != d ? UNDEF : val.compareTo(it.dec());
  }

  @Override
  public int hash() {
    return val.intValue();
  }

  @Override
  public Object java() {
    switch(type) {
      case ULN:
        return new BigInteger(val.toString());
      default:
        return val;
    }
  }

  /**
   * Converts the given double into a decimal value.
   * @param val value to be converted
   * @return double value
   * @throws QueryException possible converting exception
   */
  public static BigDecimal parse(final double val) throws QueryException {
    if(val != val || val == 1 / 0d || val == -1 / 0d) Err.value(Type.DEC, val);
    return BigDecimal.valueOf(val);
  }

  /**
   * Converts the given token into a decimal value.
   * @param val value to be converted
   * @return double value
   * @throws QueryException possible converting exception
   */
  public static BigDecimal parse(final byte[] val) throws QueryException {
    if(contains(val, 'e') || contains(val, 'E')) Err.or(FUNCAST, Type.DEC, val);
    try {
      return new BigDecimal(string(val).trim());
    } catch(final NumberFormatException ex) {
      ZERO.castErr(val);
      return null;
    }
  }
}
