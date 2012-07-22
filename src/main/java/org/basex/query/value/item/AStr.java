package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /**
   * Constructor.
   */
  protected AStr() {
    super(AtomType.STR);
  }

  /**
   * Constructor, specifying a type.
   * @param t atomic type
   */
  protected AStr(final AtomType t) {
    super(t);
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(string(ii), it.string(ii));
  }

  @Override
  public final int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(string(ii), it.string(ii));
  }
}
