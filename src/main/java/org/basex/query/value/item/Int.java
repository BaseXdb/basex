package org.basex.query.value.item;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Integer item ({@code xs:int}, {@code xs:integer}, {@code xs:short}, etc.).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Int extends ANum {
  /** Constant values. */
  private static final Int[] NUMS;
  /** Integer value. */
  private final long val;

  // caches the first 128 integers
  static {
    NUMS = new Int[128];
    for(int i = 0; i < NUMS.length; ++i) NUMS[i] = new Int(i);
  }

  /**
   * Constructor.
   * @param v value
   */
  private Int(final long v) {
    this(v, AtomType.ITR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  public Int(final long v, final Type t) {
    super(t);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Int get(final long v) {
    return v >= 0 && v < NUMS.length ? NUMS[(int) v] : new Int(v);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @param t data type
   * @return instance
   */
  public static Int get(final long v, final Type t) {
    return t == AtomType.ITR ? get(v) : new Int(v, t);
  }

  @Override
  public byte[] string() {
    return val == 0 ? Token.ZERO : Token.token(val);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return val != 0;
  }

  @Override
  public long itr() {
    return val;
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
  public Item test(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return val == ctx.pos ? this : null;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return BigDecimal.valueOf(val);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return it instanceof Int ? val == ((Int) it).val : val == it.dbl(ii);
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    if(it instanceof Int) {
      final long i = ((Int) it).val;
      return val < i ? -1 : val > i ? 1 : 0;
    }
    final double n = it.dbl(ii);
    return Double.isNaN(n) ? UNDEF : val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYT: return (byte) val;
      case SHR:
      case UBY: return (short) val;
      case INT:
      case USH: return (int) val;
      case LNG:
      case UIN: return val;
      default:  return new BigInteger(toString());
    }
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Int)) return false;
    final Int i = (Int) cmp;
    return type == i.type && val == i.val;
  }

  /**
   * Converts the given item into a long value.
   * @param val value to be converted
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  public static long parse(final byte[] val, final InputInfo ii) throws QueryException {
    // try fast conversion
    final long l = Token.toLong(val);
    if(l != Long.MIN_VALUE) return l;
    // fails; choose default conversion
    try {
      return Long.parseLong(Token.string(val).trim());
    } catch(final NumberFormatException ex) {
      throw NUMS[0].castErr(val, ii);
    }
  }
}
