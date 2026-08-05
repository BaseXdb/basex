package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbName extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final DBNode dbnode = toDBNode(toNode(arg(0), qc), false);
    return Str.get(dbnode.data().meta.name);
  }
}
