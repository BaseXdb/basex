package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-12, BSD License
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

  /**
   * Constructor.
   * @param v value
   */
  public Atm(final String v) {
    this(Token.token(v));
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return !it.type.isUntyped() ? it.eq(ii, this) : Token.eq(val, it.string(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return !it.type.isUntyped() ? -it.diff(ii, this) : Token.diff(val, it.string(ii));
  }
}
