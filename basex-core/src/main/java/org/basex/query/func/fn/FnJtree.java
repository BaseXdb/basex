package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJtree extends StandardFunc {
  @Override
  protected JNode item(final QueryContext qc) throws QueryException {
    final Item item = arg(0).unwrappedItem(qc, info);
    if(!(item instanceof XQStruct)) throw typeError(item, Types.MAP_OR_ARRAY, info);

    return new JNode(item);
  }
}
