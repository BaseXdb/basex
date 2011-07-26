package org.basex.query.item;

import static org.basex.util.Token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Integer item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class Itr extends Num {
  /** Constant values. */
  private static final Itr[] NUMS;
  /** Integer value. */
  private final long val;

  // caches the first 128 integers
  static {
    NUMS = new Itr[128];
    for(int i = 0; i < NUMS.length; ++i) NUMS[i] = new Itr(i);
  }

  /**
   * Constructor.
   * @param v value
   */
  private Itr(final long v) {
    this(v, AtomType.ITR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  public Itr(final long v, final Type t) {
    super(t);
    val = v;
  }

  /**
   * Constructor.
   * @param d date time
   */
  Itr(final Date d) {
    this(d.xc.toGregorianCalendar().getTimeInMillis(), AtomType.LNG);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Itr get(final long v) {
    return v >= 0 && v < NUMS.length ? NUMS[(int) v] : new Itr(v);
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @param t data type
   * @return instance
   */
  public static Itr get(final long v, final Type t) {
    return t == AtomType.ITR ? get(v) : new Itr(v, t);
  }

  @Override
  public final byte[] atom(final InputInfo ii) {
    return val == 0 ? Token.ZERO : Token.token(val);
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return val != 0;
  }

  @Override
  public final long itr(final InputInfo ii) {
    return val;
  }

  @Override
  public final float flt(final InputInfo ii) {
    return val;
  }

  @Override
  public final double dbl(final InputInfo ii) {
    return val;
  }

  @Override
  public final BigDecimal dec(final InputInfo ii) {
    return BigDecimal.valueOf(val);
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    return it instanceof Itr ? val == ((Itr) it).val : val == it.dbl(ii);
  }

  @Override
  public final int diff(final InputInfo ii, final Item it)
      throws QueryException {

    if(it instanceof Itr) {
      final long i = ((Itr) it).val;
      return val < i ? -1 : val > i ? 1 : 0;
    }
    final double n = it.dbl(ii);
    return Double.isNaN(n) ? UNDEF : val < n ? -1 : val > n ? 1 : 0;
  }

  @Override
  public final Object toJava() {
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
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Itr)) return false;
    final Itr i = (Itr) cmp;
    return type == i.type && val == i.val;
  }

  /**
   * Converts the given item into a long value.
   * @param val value to be converted
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  static long parse(final byte[] val, final InputInfo ii)
      throws QueryException {

    // try fast conversion
    final long l = toLong(val);
    if(l != Long.MIN_VALUE) return l;

    try {
      final String v = string(Token.trim(val));
      return Long.parseLong(v.startsWith("+") ? v.substring(1) : v);
    } catch(final NumberFormatException ex) {
      throw NUMS[0].castErr(val, ii);
    }
  }
}
