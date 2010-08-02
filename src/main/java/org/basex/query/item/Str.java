package org.basex.query.item;

import org.basex.core.Main;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    this(v, Type.STR);
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
  public final byte[] atom() {
    return val;
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return atom().length != 0;
  }

  @Override
  @SuppressWarnings("unused")
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(val, it.atom());
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(val, it.atom());
  }

  @Override
  public final String toString() {
    return Main.info("\"%\"", val);
  }
}
