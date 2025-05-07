package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnGenerateId extends ContextFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(context(qc), qc);
    if(node == null) return Str.EMPTY;

    final TokenBuilder tb = new TokenBuilder(Token.ID);
    if(node instanceof final DBNode dbnode) {
      tb.addInt(dbnode.data().dbid).add('d').addInt(dbnode.pre());
    } else {
      tb.addInt(node.id);
    }
    return Str.get(tb.finish());
  }
}
