package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Decimal item ({@code xs:decimal}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
  /** Maximum long value. */
  public static final BigDecimal BD_MINLONG = BigDecimal.valueOf(Long.MIN_VALUE);
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
  public Dec round(final int prec, final RoundMode mode) {
    if(value.signum() == 0) return this;
    final BigDecimal v = round(value, prec, mode);
    return v.equals(value) ? this : get(v);
  }

  /**
   * Returns a rounded value.
   * @param value decimal value
   * @param prec precision
   * @param mode rounding mode
   * @return rounded value
   */
  static BigDecimal round(final BigDecimal value, final int prec, final RoundMode mode) {
    if(prec >= value.scale()) return value;

    final BigDecimal l = value.setScale(prec, RoundingMode.FLOOR);
    final BigDecimal u = value.setScale(prec, RoundingMode.CEILING);
    final boolean pos = value.signum() >= 0;
    final BooleanSupplier mw = () -> l.add(u).divide(BigDecimal.valueOf(2)).compareTo(value) == 0;
    final Supplier<BigDecimal> n = () -> value.setScale(prec, RoundingMode.HALF_UP);
    return switch(mode) {
      case FLOOR -> l;
      case CEILING -> u;
      case TOWARD_ZERO -> pos ? l : u;
      case AWAY_FROM_ZERO -> pos ? u : l;
      case HALF_TO_FLOOR -> mw.getAsBoolean() ? l : n.get();
      case HALF_TO_CEILING -> mw.getAsBoolean() ? u : n.get();
      case HALF_TOWARD_ZERO -> mw.getAsBoolean() ? pos ? l : u : n.get();
      case HALF_AWAY_FROM_ZERO -> mw.getAsBoolean() ? pos ? u : l : n.get();
      default -> value.setScale(prec, RoundingMode.HALF_EVEN);
    };
  }

  @Override
  public boolean equal(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    final Item it = untypedToDec(item, ii);
    return !(it instanceof Dbl || it instanceof Flt) || Double.isFinite(it.dbl(ii)) ?
      value.compareTo(it.dec(ii)) == 0 :
      false;
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    final Item it = untypedToDec(item, ii);
    if(it instanceof Dbl || it instanceof Flt) {
      final double n = it.dbl(ii);
      if(!Double.isFinite(n)) {
        return n == Double.NEGATIVE_INFINITY ? 1 : n == Double.POSITIVE_INFINITY ? -1 :
          transitive ? 1 : NAN_DUMMY;
      }
    }
    return value.compareTo(it.dec(ii));
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public int hashCode() {
    if(value.stripTrailingZeros().scale() <= 0) {
      final BigInteger bi = value.toBigInteger();
      if(bi.bitLength() < 32) return bi.intValue();
    }
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Dec dec && value.compareTo(dec.value) == 0;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token into a decimal value.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final byte[] value, final InputInfo info) throws QueryException {
    try {
      if(!contains(value, 'e') && !contains(value, 'E'))
        return new BigDecimal(Token.string(value).trim());
    } catch(final NumberFormatException ex) { Util.debug(ex); }
    throw AtomType.DECIMAL.castError(value, info);
  }
}
