package org.basex.query.value.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Decimal item ({@code xs:decimal}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
  /** Maximum unsigned long values. */
  public static final BigDecimal MAXULNG = new BigDecimal(Long.MAX_VALUE).multiply(
      BigDecimal.valueOf(2)).add(BigDecimal.ONE);

  /** Zero value. */
  private static final Dec ZERO = new Dec(BigDecimal.ZERO);
  /** Decimal value. */
  private final BigDecimal val;

  /**
   * Constructor.
   * @param t string representation
   */
  public Dec(final byte[] t) {
    super(AtomType.DEC);
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
    super(AtomType.DEC);
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
  public byte[] string() {
    return chopNumber(token(val.toPlainString()));
  }

  @Override
  public boolean bool(final InputInfo ii) {
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
  public BigDecimal dec(final InputInfo ii) {
    return val;
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type == AtomType.DBL || it.type == AtomType.FLT ?
        it.eq(this, coll, ii) : val.compareTo(it.dec(ii)) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double d = it.dbl(ii);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : val.compareTo(it.dec(ii));
  }

  @Override
  public Object toJava() {
    return type == AtomType.ULN ? new BigInteger(val.toString()) : val;
  }

  /**
   * Converts the given double into a decimal value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final double val, final InputInfo ii) throws QueryException {
    if(Double.isNaN(val) || Double.isInfinite(val)) throw valueError(ii, AtomType.DEC, val);
    return BigDecimal.valueOf(val);
  }

  /**
   * Converts the given token into a decimal value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final byte[] val, final InputInfo ii) throws QueryException {
    if(contains(val, 'e') || contains(val, 'E')) throw FUNCAST.get(ii, AtomType.DEC, val);

    try {
      return new BigDecimal(Token.string(val).trim());
    } catch(final NumberFormatException ex) {
      throw FUNCAST.get(ii, ZERO.type, val);
    }
  }
}
