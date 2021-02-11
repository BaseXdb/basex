package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnName extends FnNodeName {
  @Override
  public final Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(ctxArg(0, qc), qc);
    return node == null || empty(node.type) ? Str.EMPTY : Str.get(name(node));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return empty(cc, false) ? Str.EMPTY : this;
  }

  /**
   * Returns the name of the specified node.
   * @param node node
   * @return name
   */
  byte[] name(final ANode node) {
    return node.name();
  }
}
