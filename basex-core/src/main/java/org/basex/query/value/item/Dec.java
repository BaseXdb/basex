package org.basex.query.value.item;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Decimal item ({@code xs:decimal}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Dec extends ANum {
  /** Maximum unsigned long values. */
  public static final BigDecimal MAXULNG = new BigDecimal(Long.MAX_VALUE).multiply(
      BigDecimal.valueOf(2)).add(BigDecimal.ONE);

  /** Zero value. */
  private static final Dec ZERO = new Dec(BigDecimal.ZERO);
  /** Decimal value. */
  private final BigDecimal value;

  /**
   * Constructor.
   * @param t string representation
   */
  public Dec(final byte[] t) {
    super(AtomType.DEC);
    value = new BigDecimal(Token.string(trim(t)));
  }

  /**
   * Constructor.
   * @param value decimal value
   * @param type string representation
   */
  public Dec(final BigDecimal value, final Type type) {
    super(type);
    this.value = value;
  }

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
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type == AtomType.DBL || it.type == AtomType.FLT ?
        it.eq(this, coll, ii) : value.compareTo(it.dec(ii)) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double d = it.dbl(ii);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : value.compareTo(it.dec(ii));
  }

  @Override
  public Object toJava() {
    return type == AtomType.ULN ? new BigInteger(value.toString()) : value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Dec && value.compareTo(((Dec) cmp).value) == 0;
  }

  /**
   * Converts the given double into a decimal value.
   * @param value value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public static BigDecimal parse(final double value, final InputInfo ii) throws QueryException {
    if(Double.isNaN(value) || Double.isInfinite(value)) throw valueError(ii, AtomType.DEC, value);
    return BigDecimal.valueOf(value);
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

    throw FUNCAST.get(ii, AtomType.DEC, chop(value, ii));
  }
}
