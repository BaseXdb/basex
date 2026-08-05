package org.basex.query.func.db;

import org.basex.data.*;
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
public final class DbPath extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    XNode node, parent = toNode(arg(0), qc);
    do {
      node = parent;
      parent = node.parent();
    } while(parent != null);

    final DBNode dbnode = toDBNode(node, false);
    return dbnode.dbKind() == Data.DOC ? Str.get(dbnode.data().text(dbnode.pre(), true)) :
      Str.EMPTY;
  }
}
