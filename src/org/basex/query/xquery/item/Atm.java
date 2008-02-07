package org.basex.query.xquery.item;

import org.basex.query.xquery.XQException;
import org.basex.util.Token;

/**
 * Untyped atomic item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public boolean eq(final Item it) throws XQException {
    return it.type != type ? it.eq(this) : Token.eq(val, it.str());
  }

  @Override
  public int diff(final Item it) throws XQException {
    return it.type != type ? -it.diff(this) : Token.diff(val, it.str());
  }
}
