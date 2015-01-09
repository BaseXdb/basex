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
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Delete expression.
 *
 * @author BaseX Team 2005-15, BSD License
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
    final Iter t = qc.iter(exprs[0]);
    for(Item i; (i = t.next()) != null;) {
      if(!(i instanceof ANode)) throw UPTRGDELEMPT.get(info);
      final ANode n = (ANode) i;
      // nodes without parents are ignored
      if(n.parent() == null) continue;
      final Updates updates = qc.resources.updates();
      final DBNode dbn = updates.determineDataRef(n, qc);
      updates.add(new DeleteNode(dbn.pre, dbn.data, info), qc);
    }
    return null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Delete(sc, info, exprs[0].copy(qc, scp, vs));
  }

  @Override
  public String toString() {
    return DELETE + ' ' + NODES + ' ' + exprs[0];
  }
}
