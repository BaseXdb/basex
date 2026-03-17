package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJtree extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = arg(0).unwrappedItem(qc, info);
    if(!(item instanceof final XQStruct struct))
      throw typeError(item, Types.MAP_OR_ARRAY, info);

    return new JNode(struct);
  }
}
