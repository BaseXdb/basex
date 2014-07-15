package org.basex.query.value.item;

import static java.lang.Double.*;
import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Float item ({@code xs:float}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Flt extends ANum {
  /** Value "NaN". */
  public static final Flt NAN = new Flt(Float.NaN);
  /** Value "0". */
  private static final Flt ZERO = new Flt(0);
  /** Value "1". */
  private static final Flt ONE = new Flt(1);
  /** Data. */
  private final float value;

  /**
   * Constructor.
   * @param value value
   */
  private Flt(final float value) {
    super(AtomType.FLT);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Flt get(final float value) {
    return value == 0 && Float.floatToRawIntBits(value) == 0 ? ZERO : value == 1 ? ONE :
      isNaN(value) ? NAN : new Flt(value);
  }

  @Override
  public byte[] string() {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !isNaN(value) && value != 0;
  }

  @Override
  public long itr() {
    return (long) value;
  }

  @Override
  public float flt() {
    return value;
  }

  @Override
  public double dbl() {
    return value;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(value, ii);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type == AtomType.DBL ? it.eq(this, coll, ii) : value == it.flt(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double n = it.flt(ii);
    if(isNaN(n) || isNaN(value)) return UNDEF;
    return value < n ? -1 : value > n ? 1 : 0;
  }

  @Override
  public Float toJava() {
    return value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Flt && value == ((Flt) cmp).value ||
      this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a double value.
   * @param value value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  static float parse(final byte[] value, final InputInfo ii) throws QueryException {
    try {
      return Float.parseFloat(Token.string(value));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(value), Token.INF)) return Float.POSITIVE_INFINITY;
      if(Token.eq(Token.trim(value), Token.NINF)) return Float.NEGATIVE_INFINITY;
      throw FUNCAST.get(ii, ZERO.type, chop(value));
    }
  }
}
