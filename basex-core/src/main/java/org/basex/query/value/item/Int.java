package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
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
    NUMS = new Int[128];
    for(int i = 0; i < NUMS.length; ++i) NUMS[i] = new Int(i);
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
   * @param type data type
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
   * @param type data type
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
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it instanceof Int ? value == ((Int) it).value : value == it.dbl(ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
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
      throw FUNCAST.get(ii, NUMS[0].type, chop(val));
    }
  }
}
