package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /**
   * Constructor.
   */
  AStr() {
    super(AtomType.STR);
  }

  /**
   * Constructor, specifying a type.
   * @param t atomic type
   */
  AStr(final AtomType t) {
    super(t);
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return coll == null ? Token.eq(string(ii), it.string(ii)) :
      coll.compare(string(ii), it.string(ii)) == 0;
  }

  @Override
  public final int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return coll == null ? Token.diff(string(ii), it.string(ii)) :
      coll.compare(string(ii), it.string(ii));
  }
}
