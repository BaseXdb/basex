package org.basex.query.func.fn;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnFloor extends Num {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;

    if(it.type.isUntyped()) it = Dbl.get(it.dbl(ii));
    else if(!(it instanceof ANum)) throw numberError(this, it);
    return ((ANum) it).floor();
  }
}
