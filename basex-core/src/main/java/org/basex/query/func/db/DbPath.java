package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DbPath extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ANode node, par = toNode(exprs[0], qc);
    do {
      node = par;
      par = node.parent();
    } while(par != null);
    final DBNode dbn = toDBNode(node);
    return Str.get(dbn.data.text(dbn.pre, true));
  }
}
