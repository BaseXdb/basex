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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnBaseUri extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(context(qc), qc);

    final Uri uri = uri(node, sc.baseURI(), info);
    return uri == null ? Empty.VALUE : uri;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }

  /**
   * Returns the static base URI of a node.
   * @param node node (can be {@code null})
   * @param staticBase static base URI (can be {@code null})
   * @param info input info (can be {@code null})
   * @return base URI or {@code null}
   * @throws QueryException query exception
   */
  public static Uri uri(final ANode node, final Uri staticBase, final InputInfo info)
      throws QueryException {

    if(node == null) return null;
    final Type type = node.type;
    if(!type.oneOf(NodeType.ELEMENT, NodeType.DOCUMENT_NODE) && node.parent() == null) return null;

    Uri base = Uri.EMPTY;
    ANode nd = node;
    do {
      if(nd == null) return staticBase != null ? staticBase.resolve(base, info) : Uri.EMPTY;
      final Uri bu = Uri.get(nd.baseURI(), false);
      if(!bu.isValid()) throw INVURI_X.get(info, nd.baseURI());
      base = bu.resolve(base, info);
      if(nd.type == NodeType.DOCUMENT_NODE && nd instanceof DBNode) break;
      nd = nd.parent();
    } while(!base.isAbsolute());
    return base;
  }
}
