package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnInnermost extends FnOutermost {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final GNodeBuilder list = nodes(qc);
    final int len = list.size();
    if(len < 2) return list.value(this).iter();

    // skip ancestors of the next node (nodes are in document order); the ancestor axis is
    // checked, as attributes and nodes from the item axis are no descendants of their parents
    final GNodeBuilder builder = new GNodeBuilder();
    for(int l = 0; l < len; l++) {
      final GNode node = list.get(l);
      if(l + 1 == len || !ancestor(node, list.get(l + 1), qc)) builder.add(node);
    }
    return builder.value(this).iter();
  }
}
