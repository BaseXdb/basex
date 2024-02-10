package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnNamespaceUri extends ContextFn {
  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(context(qc), qc);
    final QNm qname = node != null ? node.qname() : null;
    return qname != null ? Uri.get(qname.uri(), false) : Uri.EMPTY;
  }
}
