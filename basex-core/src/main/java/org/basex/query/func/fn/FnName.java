package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnName extends FnNodeName {
  @Override
  public final Str value(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    return node == null || empty(node.kind()) ? Str.EMPTY : Str.get(name(node));
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
  byte[] name(final XNode node) {
    return node.name();
  }
}
