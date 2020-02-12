package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnBaseUri extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toEmptyNode(ctxArg(0, qc), qc);
    if(node == null || node.type != NodeType.ELM && node.type != NodeType.DOC &&
        node.parent() == null) return Empty.VALUE;

    Uri base = Uri.EMPTY;
    ANode nd = node;
    do {
      if(nd == null) return sc.baseURI().resolve(base, info);
      final Uri bu = Uri.uri(nd.baseURI(), false);
      if(!bu.isValid()) throw INVURI_X.get(info, nd.baseURI());
      base = bu.resolve(base, info);
      if(nd.type == NodeType.DOC && nd instanceof DBNode) break;
      nd = nd.parent();
    } while(!base.isAbsolute());
    return base;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
