package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Float item ({@code xs:float}).
 *
 * @author BaseX Team 2005-15, BSD License
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
      Float.isNaN(value) ? NAN : new Flt(value);
  }

  @Override
  protected byte[] string() {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !Float.isNaN(value) && value != 0;
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
    if(Float.isNaN(value) || Float.isInfinite(value)) throw valueError(ii, AtomType.DEC, string());
    return new BigDecimal(value);
  }

  @Override
  public Flt abs() {
    return value > 0d || 1 / value > 0 ? this : get(-value);
  }

  @Override
  public Flt ceiling() {
    final float v = (float) Math.ceil(value);
    return v == value ? this : get(v);
  }

  @Override
  public Flt floor() {
    final float v = (float) Math.floor(value);
    return v == value ? this : get(v);
  }

  @Override
  public Flt round(final int scale, final boolean even) {
    final float v = Dbl.get(value).round(scale, even).flt();
    return value == v ? this : get(v);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it.type == AtomType.DBL ? it.eq(this, coll, sc, ii) : value == it.flt(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    final float n = it.flt(ii);
    return Float.isNaN(n) || Float.isNaN(value) ? UNDEF : value < n ? -1 : value > n ? 1 : 0;
  }

  @Override
  public Float toJava() {
    return value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Flt && value == ((Flt) cmp).value || this == NAN && cmp == NAN;
  }

  /**
   * Converts the given token into a float value.
   * @param value value to be converted
   * @param ii input info
   * @return float value
   * @throws QueryException query exception
   */
  static float parse(final byte[] value, final InputInfo ii) throws QueryException {
    try {
      return Float.parseFloat(Token.string(value));
    } catch(final NumberFormatException ex) {
      final byte[] v = Token.trim(value);
      if(Token.eq(v, Token.INF)) return Float.POSITIVE_INFINITY;
      if(Token.eq(v, Token.NINF)) return Float.NEGATIVE_INFINITY;
      throw funCastError(ii, AtomType.FLT, value);
    }
  }
}
