package org.basex.query.func.fn;

import org.basex.core.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnNilled extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toEmptyNode(arg(0, qc), qc);
    // always false, as no schema information is given
    return node == null || node.type != NodeType.ELM ? null : Bln.FALSE;
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
