package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Decimal item ({@code xs:decimal}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
  /** Maximum long value. */
  public static final BigDecimal BD_MAXLONG = BigDecimal.valueOf(Long.MAX_VALUE);
  /** Decimal representing a million. */
  public static final BigDecimal BD_1000000 = BigDecimal.valueOf(1000000);
  /** Seconds per day. */
  public static final BigDecimal BD_864000 = BigDecimal.valueOf(86400);
  /** BigDecimal: 146097. */
  public static final BigDecimal BD_146097 = BigDecimal.valueOf(146097);
  /** BigDecimal: 36525. */
  public static final BigDecimal BD_36525 = BigDecimal.valueOf(36525);
  /** BigDecimal: 36524. */
  public static final BigDecimal BD_36524 = BigDecimal.valueOf(36524);
  /** BigDecimal: 60. */
  public static final BigDecimal BD_3600 = BigDecimal.valueOf(3600);
  /** BigDecimal: 1461. */
  public static final BigDecimal BD_1461 = BigDecimal.valueOf(1461);
  /** BigDecimal: 1000. */
  public static final BigDecimal BD_1000 = BigDecimal.valueOf(1000);
  /** BigDecimal: 366. */
  public static final BigDecimal BD_366 = BigDecimal.valueOf(366);
  /** BigDecimal: 365. */
  public static final BigDecimal BD_365 = BigDecimal.valueOf(365);
  /** BigDecimal: 153. */
  public static final BigDecimal BD_153 = BigDecimal.valueOf(153);
  /** BigDecimal: 100. */
  public static final BigDecimal BD_100 = BigDecimal.valueOf(100);
  /** BigDecimal: 60. */
  public static final BigDecimal BD_60 = BigDecimal.valueOf(60);
  /** BigDecimal: 5. */
  public static final BigDecimal BD_5 = BigDecimal.valueOf(5);
  /** BigDecimal: 4. */
  public static final BigDecimal BD_4 = BigDecimal.valueOf(4);
  /** BigDecimal: 2. */
  public static final BigDecimal BD_2 = BigDecimal.valueOf(2);

  /** Value 0. */
  public static final Dec ZERO = new Dec(BigDecimal.ZERO);
  /** Value 1. */
  public static final Dec ONE = new Dec(BigDecimal.ONE);
  /** Decimal value. */
  private final BigDecimal value;

  /**
   * Constructor.
   * @param value decimal value
   */
  private Dec(final BigDecimal value) {
    super(AtomType.DECIMAL);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value big decimal value
   * @return value
   */
  public static Dec get(final BigDecimal value) {
    return value.signum() == 0 ? ZERO : new Dec(value);
  }

  @Override
  public byte[] string() {
    return chopNumber(token(value.toPlainString()));
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.signum() != 0;
  }

  @Override
  public long itr() {
    return value.longValue();
  }

  @Override
  public float flt() {
    return value.floatValue();
  }

  @Override
  public double dbl() {
    return value.doubleValue();
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return value;
  }

  @Override
  public Dec abs() {
    return value.signum() == -1 ? get(value.negate()) : this;
  }

  @Override
  public Dec ceiling() {
    return get(value.setScale(0, RoundingMode.CEILING));
  }

  @Override
  public Dec floor() {
    return get(value.setScale(0, RoundingMode.FLOOR));
  }

  @Override
  public Dec round(final int scale, final boolean even) {
    final int s = value.signum();
    return s == 0 ? this : get(value.setScale(scale, even ? RoundingMode.HALF_EVEN :
           s == 1 ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN));
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final Type t = item.type;
    return t.isUntyped() ? dbl() == item.dbl(ii) :
      t == AtomType.DOUBLE || t == AtomType.FLOAT ? item.eq(this, coll, sc, ii) :
      value.compareTo(item.dec(ii)) == 0;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    final double d = item.dbl(ii);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : value.compareTo(item.dec(ii));
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Dec && value.compareTo(((Dec) obj).value) == 0;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token into a decimal value.
   * @param item item to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final Item item, final InputInfo ii) throws QueryException {
    final byte[] value = item.string(ii);
    try {
      if(!contains(value, 'e') && !contains(value, 'E'))
        return new BigDecimal(Token.string(value).trim());
    } catch(final NumberFormatException ex) { Util.debug(ex); }
    throw AtomType.DECIMAL.castError(item, ii);
  }
}
