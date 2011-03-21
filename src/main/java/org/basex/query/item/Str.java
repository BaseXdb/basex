package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * String item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class Str extends Item {
  /** String data. */
  public static final Str ZERO = new Str(Token.EMPTY);
  /** String data. */
  protected final byte[] val;

  /**
   * Constructor.
   * @param v value
   */
  private Str(final byte[] v) {
    this(v, AtomType.STR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  protected Str(final byte[] v, final Type t) {
    super(t);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Str get(final byte[] v) {
    return v.length == 0 ? ZERO : new Str(v);
  }

  /**
   * Returns an instance of this class.
   * @param v object (will be converted to token)
   * @return instance
   */
  public static Str get(final Object v) {
    return get(Token.token(v.toString()));
  }

  @Override
  public final byte[] atom(final InputInfo ii) {
    return val;
  }

  /**
   * Returns an atomized string.
   * @return Returns an atomized string.
   */
  public final byte[] atom() {
    return atom(null);
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return atom(ii).length != 0;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(val, it.atom(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(val, it.atom(ii));
  }

  @Override
  public SeqType type() {
    return SeqType.STR;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Str)) return false;
    final Str i = (Str) cmp;
    return type == i.type && Token.eq(val, i.val);
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", val);
  }

  @Override
  public String toJava() {
    return Token.string(val);
  }
}
