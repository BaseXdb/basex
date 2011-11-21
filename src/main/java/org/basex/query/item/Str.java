package org.basex.query.item;

import static org.basex.data.DataText.*;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.list.ByteList;

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
  public final byte[] string(final InputInfo ii) {
    return val;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public final byte[] string() {
    return val;
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return string(ii).length != 0;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(val, it.string(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(val, it.string(ii));
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Str)) return false;
    final Str i = (Str) cmp;
    return type == i.type && Token.eq(val, i.val);
  }

  @Override
  public final String toJava() {
    return Token.string(val);
  }

  @Override
  public boolean isString() {
    return true;
  }

  @Override
  public final String toString() {
    final ByteList tb = new ByteList();
    tb.add('"');
    for(final byte v : val) {
      if(v == '&') tb.add(E_AMP);
      else tb.add(v);
      if(v == '"') tb.add(v);
    }
    return tb.add('"').toString();
  }
}
