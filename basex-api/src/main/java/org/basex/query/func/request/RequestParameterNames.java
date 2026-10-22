package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameterNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final HashItemSet names = new HashItemSet(ItemSet.Mode.ATOMIC, info);
    for(final Item name : queryValues(qc).keys()) names.add(name);
    for(final Item name : formValues(qc).keys()) names.add(name);
    return ItemSeq.get(names.keys(), names.size(), null);
  }
}
