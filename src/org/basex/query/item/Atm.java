package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * Untyped atomic item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Atm extends Str {
  /**
   * Constructor.
   * @param v value
   */
  public Atm(final byte[] v) {
    super(v, Type.ATM);
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    return it.type != type ? it.eq(this) : Token.eq(val, it.str());
  }

  @Override
  public int diff(final Item it) throws QueryException {
    return it.type != type ? -it.diff(this) : Token.diff(val, it.str());
  }
}
