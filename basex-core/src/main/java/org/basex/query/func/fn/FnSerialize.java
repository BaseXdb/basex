package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnSerialize extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs.length > 1 ? exprs[1].item(qc, info) : null;
    final SerializerOptions sopts = FuncOptions.serializer(it, info);
    return Str.get(serialize(exprs[0].iter(qc), sopts, SER_X));
  }
}
