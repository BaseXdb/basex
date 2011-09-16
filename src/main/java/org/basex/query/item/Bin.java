package org.basex.query.item;

import java.io.IOException;
import java.io.InputStream;

import org.basex.io.in.ArrayInput;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;

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

  /**
   * Returns the binary content.
   * @param ii input info
   * @return content
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected byte[] val(final InputInfo ii) throws QueryException {
    return val;
  }

  @Override
  public InputStream input() throws IOException {
    return new ArrayInput(val);
  }

  @Override
  public final byte[] toJava() throws QueryException {
    return val(null);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Bin)) return false;
    final Bin b = (Bin) cmp;
    return type == b.type && Token.eq(val, b.val);
  }
}
