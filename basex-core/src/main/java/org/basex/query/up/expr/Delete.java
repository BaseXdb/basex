package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Delete expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param trg target expression
   */
  public Delete(final StaticContext sc, final InputInfo info, final Expr trg) {
    super(sc, info, trg);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(!(item instanceof ANode)) throw UPTRGDELEMPT_X.get(info, item);
      final ANode node = (ANode) item;
      // nodes without parents are ignored
      if(node.parent() == null) continue;
      final Updates updates = qc.updates();
      final DBNode dbn = updates.determineDataRef(node, qc);
      updates.add(new DeleteNode(dbn.pre(), dbn.data(), info), qc);
    }
    return Empty.VALUE;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Delete(sc, info, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Delete && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(DELETE).token(NODES).token(exprs[0]);
  }
}
