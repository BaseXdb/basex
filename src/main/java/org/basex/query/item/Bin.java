package org.basex.query.item;

import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Base64Binary item. Derived from java.util.prefs.Base64.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Bin extends Item {
  /** Binary data. */
  protected byte[] val;

  /**
   * Constructor.
   * @param d binary data
   * @param t type
   */
  protected Bin(final byte[] d, final Type t) {
    super(t);
    val = d;
  }

  @Override
  public final byte[] atom(final InputInfo ii) {
    return atom();
  }

  /**
   * Returns an atomized string.
   * @return string representation
   */
  public abstract byte[] atom();

  @Override
  public final byte[] toJava() {
    return val;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Bin)) return false;
    final Bin b = (Bin) cmp;
    return type == b.type && Token.eq(val, b.val);
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", atom());
  }
}
