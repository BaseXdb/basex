package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Decimal item ({@code xs:decimal}).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
  /** Zero value. */
  private static final Dec ZERO = new Dec(BigDecimal.ZERO);
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
    return get(BigDecimal.valueOf(value));
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
    return get(value.setScale(0, BigDecimal.ROUND_CEILING));
  }

  @Override
  public Dec floor() {
    return get(value.setScale(0, BigDecimal.ROUND_FLOOR));
  }

  @Override
  public Dec round(final int scale, final boolean even) {
    final int s = value.signum();
    return s == 0 ? this : get(value.setScale(scale, even ? BigDecimal.ROUND_HALF_EVEN :
    s == 1 ? BigDecimal.ROUND_HALF_UP : BigDecimal.ROUND_HALF_DOWN));
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it.type == AtomType.DBL || it.type == AtomType.FLT ?
        it.eq(this, coll, sc, ii) : value.compareTo(it.dec(ii)) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    final double d = it.dbl(ii);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : value.compareTo(it.dec(ii));
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Dec && value.compareTo(((Dec) cmp).value) == 0;
  }

  /**
   * Converts the given token into a decimal value.
   * @param value value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final byte[] value, final InputInfo ii) throws QueryException {
    try {
      if(!contains(value, 'e') && !contains(value, 'E'))
        return new BigDecimal(Token.string(value).trim());
    } catch(final NumberFormatException ignored) { }

    throw funCastError(ii, AtomType.DEC, value);
  }
}
