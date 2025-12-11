package org.basex.query.value.item;

import java.io.*;
import java.math.*;

import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Integer item ({@code xs:int}, {@code xs:integer}, {@code xs:short}, etc.).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Itr extends ANum {
  /** Maximum values. */
  public static final Itr MAX;
  /** Value 0. */
  public static final Itr ZERO;
  /** Value 1. */
  public static final Itr ONE;

  /** Constant values. */
  private static final Itr[] INTSS;
  /** Integer value. */
  private final long value;

  // caches the first 128 integers
  static {
    final int nl = 128;
    INTSS = new Itr[nl];
    for(int n = 0; n < nl; n++) INTSS[n] = new Itr(n);
    MAX = get(Long.MAX_VALUE);
    ZERO = INTSS[0];
    ONE = INTSS[1];
  }

  /**
   * Constructor.
   * @param value value
   */
  private Itr(final long value) {
    this(value, AtomType.INTEGER);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   */
  public Itr(final long value, final Type type) {
    super(type);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Itr get(final long value) {
    return value >= 0 && value < INTSS.length ? INTSS[(int) value] : new Itr(value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param type item type
   * @return instance
   */
  public static Itr get(final long value, final Type type) {
    return type == AtomType.INTEGER ? get(value) : new Itr(value, type);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeLong(value);
  }

  @Override
  public byte[] string() {
    return Token.token(value);
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
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos) {
    return pos > 0 ? value == pos : bool(ii);
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return BigDecimal.valueOf(value);
  }

  @Override
  public Itr abs() {
    return value < 0 ? get(-value) : this;
  }

  @Override
  public Itr ceiling() {
    return this;
  }

  @Override
  public Itr floor() {
    return this;
  }

  @Override
  public Itr round(final int prec, final RoundMode mode) {
    if(value == 0 || prec >= 0) return this;
    final long v = Dec.round(BigDecimal.valueOf(value), prec, mode).longValue();
    return v == value ? this : get(v);
  }

  /**
   * Returns a 32-bit integer.
   * @return integer or {@link Integer#MIN_VALUE}
   */
  public int toInt() {
    final int i = (int) value;
    return value == i ? i : Integer.MIN_VALUE;
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item instanceof final Itr itr ? Long.compare(value, itr.value) :
      Dec.compare(this, item, transitive, ii);
  }

  @Override
  public Object toJava() {
    return switch((AtomType) type) {
      case BYTE -> (byte) value;
      case SHORT, UNSIGNED_BYTE -> (short) value;
      case UNSIGNED_SHORT -> (char) value;
      case INT -> (int) value;
      default -> value;
    };
  }

  @Override
  public int hashCode() {
    final long v = value;
    final int i = (int) v;
    return v == i ? i : super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Itr itr && type == itr.type && value == itr.value;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the given token to a long value.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return long value
   * @throws QueryException query exception
   */
  public static long parse(final byte[] value, final InputInfo info) throws QueryException {
    final long l = Token.toLong(value);
    if(l != Long.MIN_VALUE || Token.eq(Token.trim(value), Token.MIN_LONG)) return l;
    throw AtomType.INTEGER.castError(value, info);
  }
}
