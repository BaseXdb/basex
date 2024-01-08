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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class UtilStripNamespaces extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    final Iter iter = arg(1).iter(qc);

    final TokenSet prefixes = new TokenSet();
    for(Item item; (item = qc.next(iter)) != null;) {
      prefixes.add(toToken(item));
    }
    return DataBuilder.stripNamespaces(node, prefixes, qc.context, info);
  }
}
