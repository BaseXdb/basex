package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class InspectType extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value value = exprs[0].value(qc);

    // combine types of all items to get more specific type
    Type type = null;
    for(Item item : value) {
      type = type == null ? item.type : type.union(item.type);
    }
    return Str.get(SeqType.get(type, value.seqType().occ).toString());
  }
}
