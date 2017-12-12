package org.basex.query.func.update;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class UpdateOutput extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Updates updates = qc.updates();
    if(updates.mod instanceof TransformModifier) throw BASEX_UPDATE.get(info);
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) qc.updates.items.add(item);
    return null;
  }
}
