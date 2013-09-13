package org.basex.query.value.item;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract class for binary items.
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
  public byte[] binary(final InputInfo ii) throws QueryException {
    return data;
  }

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    return new ArrayInput(data);
  }

  @Override
  public final byte[] toJava() throws QueryException {
    return binary(null);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Bin)) return false;
    final Bin b = (Bin) cmp;
    return type == b.type && Token.eq(data, b.data);
  }
}
