package org.basex.query.func.json;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class JsonDoc extends JsonParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toTokenOrNull(exprs[0], qc);
    return uri != null ? parse(checkPath(uri), qc) : Empty.VALUE;
  }
}
