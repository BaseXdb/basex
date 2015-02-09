package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnBaseUri extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toEmptyNode(arg(0, qc), qc);
    if(node == null) return null;
    if(node.type != NodeType.ELM && node.type != NodeType.DOC && node.parent() == null) return null;

    Uri base = Uri.EMPTY;
    ANode n = node;
    do {
      if(n == null) return sc.baseURI().resolve(base, info);
      final Uri bu = Uri.uri(n.baseURI(), false);
      if(!bu.isValid()) throw INVURI_X.get(ii, n.baseURI());
      base = bu.resolve(base, info);
      if(n.type == NodeType.DOC && n instanceof DBNode) break;
      n = n.parent();
    } while(!base.isAbsolute());
    return base;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
