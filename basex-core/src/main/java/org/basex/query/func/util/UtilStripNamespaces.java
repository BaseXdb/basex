package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class UtilStripNamespaces extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(exprs[0], qc);
    final TokenSet prefixes = new TokenSet();
    if(exprs.length > 1) {
      final Iter iter = exprs[1].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) prefixes.add(toToken(item));
    }
    return DataBuilder.stripNamespaces(node, prefixes, qc.context, info);
  }
}
