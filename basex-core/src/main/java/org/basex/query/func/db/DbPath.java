package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbPath extends DbAccess {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node, par = toNode(exprs[0], qc);
    do {
      node = par;
      par = node.parent();
    } while(par != null);
    final DBNode dbn = toDBNode(node);
    return dbn.kind() == Data.DOC ? Str.get(dbn.data().text(dbn.pre(), true)) : Str.ZERO;
  }
}
