package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RequestSetAttribute extends ApiFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String name = Token.string(toToken(exprs[0], qc));
    final Value value = exprs[1].value(qc);

    request(qc).setAttribute(name, value.materialize(qc, REQUEST_ATTRIBUTE_X, info));
    return Empty.VALUE;
  }
}
