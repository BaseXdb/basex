package org.basex.query.func.fn;

import org.basex.data.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDocumentUri extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(ctxArg(0, qc), qc);
    if(node == null || node.type != NodeType.DOCUMENT_NODE) return Empty.VALUE;
    // return empty sequence for documents constructed via parse-xml
    final Data data = node.data();
    if(data != null && data.meta.name.isEmpty()) return Empty.VALUE;

    final byte[] uri = node.baseURI();
    return uri.length == 0 ? Empty.VALUE : Uri.uri(uri, false);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
