package org.basex.query.item;

import java.io.InputStream;

import org.basex.io.in.ArrayInput;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Binary item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Bin extends Item {
  /** Binary data. */
  byte[] data;

  /**
   * Constructor.
   * @param d binary data
   * @param t type
   */
  Bin(final byte[] d, final Type t) {
    super(t);
    data = d;
  }

  /**
   * Returns the binary content.
   * @param ii input info
   * @return content
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  byte[] val(final InputInfo ii) throws QueryException {
    return data;
  }

  @Override
  public InputStream input(final InputInfo ii) throws QueryException {
    return new ArrayInput(data);
  }

  @Override
  public final byte[] toJava() throws QueryException {
    return val(null);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Bin)) return false;
    final Bin b = (Bin) cmp;
    return type == b.type && Token.eq(data, b.data);
  }
}
