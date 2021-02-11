package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbNodeId extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    return new Iter() {
      @Override
      public Int next() throws QueryException {
        final Item item = qc.next(iter);
        return item != null ? Int.get(id(toDBNode(item))) : null;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final LongList list = new LongList(Seq.initialCapacity(iter.size()));
    for(Item item; (item = qc.next(iter)) != null;) list.add(id(toDBNode(item)));
    return IntSeq.get(list.finish());
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    exprType.assign(seqType(), expr.seqType().occ, expr.size());
    return this;
  }

  /**
   * Returns the node value.
   * @param node database node
   * @return node id
   */
  protected int id(final DBNode node) {
    return node.data().id(node.pre());
  }
}
