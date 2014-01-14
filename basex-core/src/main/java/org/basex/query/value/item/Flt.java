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
 * @author BaseX Team 2005-13, BSD License
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
  private final float val;

  /**
   * Constructor.
   * @param v value
   */
  private Flt(final float v) {
    super(AtomType.FLT);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param f value
   * @return instance
   */
  public static Flt get(final float f) {
    return f == 0 && Float.floatToRawIntBits(f) == 0 ? ZERO : f == 1 ? ONE :
      isNaN(f) ? NAN : new Flt(f);
  }

  @Override
  public byte[] string() {
    return Token.token(val);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !isNaN(val) && val != 0;
  }

  @Override
  public long itr() {
    return (long) val;
  }

  @Override
  public float flt() {
    return val;
  }

  @Override
  public double dbl() {
    return val;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(val, ii);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type == AtomType.DBL ? it.eq(this, coll, ii) : val == it.flt(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final double n = it.flt(ii);
    if(isNaN(n) || isNaN(val)) return UNDEF;
    return val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public Float toJava() {
    return val;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Flt && val == ((Flt) cmp).val ||
      this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a double value.
   * @param val value to be converted
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  static float parse(final byte[] val, final InputInfo ii) throws QueryException {
    try {
      return Float.parseFloat(Token.string(val));
    } catch(final NumberFormatException ex) {
      if(Token.eq(Token.trim(val), Token.INF)) return Float.POSITIVE_INFINITY;
      if(Token.eq(Token.trim(val), Token.NINF)) return Float.NEGATIVE_INFINITY;
      throw FUNCAST.get(ii, ZERO.type, chop(val));
    }
  }
}
