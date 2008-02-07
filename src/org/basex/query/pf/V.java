package org.basex.query.pf;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * This is an abstract class for XQuery results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class V {
  /**
   * Returns the numeric (integer) representation of the token.
   * @return value
   */
  abstract int i();

  /**
   * Returns the boolean representation of the token.
   * @return value
   */
  abstract boolean b();

  /**
   * Returns the double representation of the token.
   * @return value
   */
  abstract double d();

  /**
   * Returns a string representation of the token.
   * @return value
   */
  abstract byte[] s();

  /**
   * Returns the difference between the current and specified value.
   * @param v value to be compared
   * @return difference
   */
  abstract int df(V v);

  /**
   * Checks if the value equals the specified value.
   * @param v value to be compared
   * @return value
   */
  abstract boolean eq(V v);

  /**
   * Returns the value type.
   * @return value type
   */
  abstract int t();

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + Token.string(s()) + "]";
  }
}


/** Boolean value. */
final class B extends V {
  /** Value. */
  static final B T = new B(true);
  /** Value. */
  static final B F = new B(false);
  /** Value. */
  private final boolean v;

  /**
   * Constructor.
   * @param b boolean value
   */
  private B(final boolean b) { v = b; }

  /**
   * Returns static value.
   * @param b boolean value
   * @return value
   */
  static B v(final boolean b) { return b ? T : F; }

  @Override
  boolean b() { return v; }

  @Override
  int i() { return Integer.MIN_VALUE; }

  @Override
  double d() { return Double.NaN; }

  @Override
  byte[] s() { return v ? Token.TRUE : Token.FALSE; }

  @Override
  int df(final V b) { return v == b.b() ? 1 : v && b == B.F ? 1 : -1; }

  @Override
  boolean eq(final V b) { return this == b; }

  @Override
  int t() { return BLN; }
}


/** Double value .*/
final class D extends V {
  /** Invalid value. */
  static final D INVALID = new D(Double.NaN);
  /** Value. */
  private final double d;

  /**
   * Constructor.
   * @param t token
   * @throws QueryException query exception
   */
  D(final byte[] t) throws QueryException {
    final double v = Token.toDouble(t);
    if(v != v) throw new QueryException("Can't cast '" + Token.string(t) + "'");
    d = v;
  }

  /**
   * Constructor.
   * @param v integer value
   */
  D(final double v) { d = v; }

  @Override
  boolean b() { return d != 0; }

  @Override
  int i() { return (int) d; }

  @Override
  double d() { return d; }

  @Override
  byte[] s() { return Token.token(d); }

  @Override
  int df(final V v) { return (int) (d - v.d()); }

  @Override
  boolean eq(final V v) { return d == v.d(); }

  @Override
  int t() { return DBL; }
}


/** Integer value. */
final class I extends V {
  /** Value. */
  private final int i;

  /**
   * Constructor.
   * @param to token
   * @throws QueryException query exception
   */
  I(final byte[] to) throws QueryException {
    // valid here?.. casting double to int, but catching strings
    final double d = Token.toDouble(to);
    if(d != d) throw new QueryException("Can't cast '" +
        Token.string(to) + "'");
    i = (int) d;
  }

  /**
   * Constructor.
   * @param v integer value
   */
  I(final int v) { i = v; }

  @Override
  boolean b() { return i != 0; }

  @Override
  int i() { return i; }

  @Override
  double d() { return i; }

  @Override
  byte[] s() { return Token.token(i); }

  @Override
  int df(final V v) { return i - v.i(); }

  @Override
  boolean eq(final V v) { return i == v.i(); }

  @Override
  int t() { return INT; }
}


/** Node value. */
final class N extends V {
  /** Constant Value. */
  static final N Z = new N(0);
  /** Value. */
  private final int n;

  /**
   * Constructor.
   * @param t token
   */
  N(final byte[] t) { this(Token.toInt(t)); }

  /**
   * Constructor.
   * @param v integer value
   */
  N(final int v) { n = v; }

  @Override
  boolean b() { return n != 0; }

  @Override
  int i() { return n; }

  @Override
  double d() { return n; }

  @Override
  byte[] s() { return Token.token(n); }

  @Override
  int df(final V v) { return n - v.i(); }

  @Override
  boolean eq(final V v) { return n == v.i(); }

  @Override
  int t() { return PRE; }
}


/** String value. */
final class S extends V {
  /** Value. */
  private final byte[] s;

  /**
   * Constructor.
   * @param v integer value
   */
  S(final byte[] v) { s = v; }

  @Override
  boolean b() { return s.length != 0; }

  @Override
  int i() { return Token.toInt(s); }

  @Override
  double d() { return Token.toDouble(s); }

  @Override
  byte[] s() { return s; }

  @Override
  int df(final V v) { return Token.diff(s, v.s()); }

  @Override
  boolean eq(final V v) { return Token.eq(s, v.s()); }

  @Override
  int t() { return STR; }
}


/** Fragment value. */
final class PFN extends V {
  /** Data reference. */
  private final Data data;
  /** Pre value. */
  final int pp;

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  PFN(final Data d, final int p) {
    data = d;
    pp = p;
  }

  @Override
  boolean eq(final V v) { return false; }

  @Override
  int i() { return 0; }

  @Override
  boolean b() { return false; }

  @Override
  double d() { return 0; }

  @Override
  byte[] s() { return null; }

  @Override
  int df(final V v) { return 0; }

  @Override
  int t() { return INT; }

  @Override
  public String toString() { return "XFrag[" + data + "]"; }
}
