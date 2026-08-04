package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilStripNamespaces extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final XNode node = toNode(arg(0), qc);
    final Value names = arg(1).atomValue(qc, info);

    final TokenSet set = new TokenSet(names.size());
    for(final Item item : names) set.add(toToken(item));
    return DataBuilder.stripNamespaces(node, set, qc.context, info);
  }
}
