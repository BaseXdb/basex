package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Untyped atomic item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Atm extends Str {
  /**
   * Constructor.
   * @param v value
   */
  public Atm(final byte[] v) {
    super(v, AtomType.ATM);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return !it.type.isUntyped() ? it.eq(ii, this) :
      Token.eq(val, it.string(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return !it.type.isUntyped() ? -it.diff(ii, this) :
      Token.diff(val, it.string(ii));
  }
}
