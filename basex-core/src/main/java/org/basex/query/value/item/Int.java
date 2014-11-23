package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Integer item ({@code xs:int}, {@code xs:integer}, {@code xs:short}, etc.).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Int extends ANum {
  /** Constant values. */
  private static final Int[] NUMS;
  /** Integer value. */
  private final long value;

  // caches the first 128 integers
  static {
    final int nl = 128;
    NUMS = new Int[nl];
    for(int n = 0; n < nl; n++) NUMS[n] = new Int(n);
  }

  /**
   * Constructor.
   * @param value value
   */
  private Int(final long value) {
    this(value, AtomType.ITR);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   */
  public Int(final long value, final Type type) {
    super(type);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Int get(final long value) {
    return value >= 0 && value < NUMS.length ? NUMS[(int) value] : new Int(value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param type item type
   * @return instance
   */
  public static Int get(final long value, final Type type) {
    return type == AtomType.ITR ? get(value) : new Int(value, type);
  }

  @Override
  public byte[] string() {
    return value == 0 ? Token.ZERO : Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value != 0;
  }

  @Override
  public long itr() {
    return value;
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
  public Item test(final QueryContext qc, final InputInfo ii) {
    return value == qc.pos ? this : null;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return BigDecimal.valueOf(value);
  }

  @Override
  public Int abs() {
    return value < 0 ? get(-value) : type != AtomType.ITR ? get(value) : this;
  }

  @Override
  public Int ceiling() {
    return this;
  }

  @Override
  public Int floor() {
    return this;
  }

  @Override
  public ANum round(final int scale, final boolean even) {
    final long v = rnd(scale, even);
    return v == value ? this : get(v);
  }

  /**
   * Returns a rounded value.
   * @param s scale
   * @param e half-to-even flag
   * @return result
   */
  private long rnd(final int s, final boolean e) {
    long v = value;
    if(s >= 0 || v == 0) return v;
    if(s < -15) return Dec.get(new BigDecimal(v)).round(s, e).itr();

    long f = 1;
    final int c = -s;
    for(long i = 0; i < c; i++) f = (f << 3) + (f << 1);
    final boolean n = v < 0;
    final long a = n ? -v : v, m = a % f, d = m << 1;
    v = a - m;
    if(e ? d > f || d == f && v % (f << 1) != 0 : n ? d > f : d >= f) v += f;
    return n ? -v : v;
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it instanceof Int ? value == ((Int) it).value : value == it.dbl(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    if(it instanceof Int) {
      final long i = ((Int) it).value;
      return value < i ? -1 : value > i ? 1 : 0;
    }
    final double n = it.dbl(ii);
    return Double.isNaN(n) ? UNDEF : value < n ? -1 : value > n ? 1 : 0;
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYT: return (byte) value;
      case SHR:
      case UBY: return (short) value;
      case INT:
      case USH: return (int) value;
      case LNG:
      case UIN: return value;
      default:  return new BigInteger(toString());
    }
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Int)) return false;
    final Int i = (Int) cmp;
    return type == i.type && value == i.value;
  }

  /**
   * Converts the given item into a long value.
   * @param value value to be converted
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  public static long parse(final byte[] value, final InputInfo ii) throws QueryException {
    final byte[] val = Token.trim(value);
    // fast check for valid characters
    boolean valid = true;
    for(final byte v : val) {
      if(!Token.digit(v) && v != '+' && v != '-') {
        valid = false;
        break;
      }
    }
    // valid: try fast conversion
    if(valid) {
      final long l = Token.toLong(val);
      if(l != Long.MIN_VALUE || Token.eq(val, Token.MINLONG)) return l;
    }
    throw funCastError(ii, AtomType.INT, val);
  }
}
