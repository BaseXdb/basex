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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
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
    super(AtomType.DEC);
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

  /**
   * Constructor.
   * @param value big decimal value
   * @return value
   */
  public static Dec get(final double value) {
    return get(new BigDecimal(value));
  }

  @Override
  public byte[] string() {
    return chopNumber(token(value.toPlainString()));
  }

  @Override
  public boolean bool(final InputInfo info) {
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
  public BigDecimal dec(final InputInfo info) {
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
      final InputInfo info) throws QueryException {
    return item.type == AtomType.DBL || item.type == AtomType.FLT ?
        item.eq(this, coll, sc, info) : value.compareTo(item.dec(info)) == 0;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    final double d = item.dbl(info);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : value.compareTo(item.dec(info));
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Dec && value.compareTo(((Dec) obj).value) == 0;
  }

  /**
   * Converts the given token into a decimal value.
   * @param item item to be converted
   * @param info input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final Item item, final InputInfo info) throws QueryException {
    final byte[] value = item.string(info);
    try {
      if(!contains(value, 'e') && !contains(value, 'E'))
        return new BigDecimal(Token.string(value).trim());
    } catch(final NumberFormatException ex) { Util.debug(ex); }
    throw AtomType.DEC.castError(item, info);
  }
}
