package org.basex.query.func.db;

import static org.basex.query.func.Function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbNodeId extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).iter(qc);
    if(nodes.size() >= 0) return ids(nodes.value(qc, null)).iter();

    return new Iter() {
      @Override
      public Int next() throws QueryException {
        final Item item = qc.next(nodes);
        return item != null ? Int.get(id(toDBNode(item, false))) : null;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return ids(arg(0).value(qc));
  }

  /**
   * Creates the result.
   * @param nodes nodes
   * @return node IDs
   * @throws QueryException query exception
   */
  final Value ids(final Value nodes) throws QueryException {
    final LongList ids = new LongList(Seq.initialCapacity(nodes.size()));
    addIds(nodes, ids);
    return IntSeq.get(ids.finish());
  }

  /**
   * Adds the IDs.
   * @param nodes nodes
   * @param ids ID list
   * @throws QueryException query exception
   */
  protected void addIds(final Value nodes, final LongList ids) throws QueryException {
    for(final Item node : nodes) ids.add(id(toDBNode(node, false)));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr nodes = arg(0);
    final Data data = nodes.data();
    if(data != null && !data.meta.updindex && !(this instanceof DbNodePre)) {
      // no ID-PRE mapping: work with PRE values
      return cc.function(_DB_NODE_PRE, info, nodes);
    }
    exprType.assign(seqType(), nodes.seqType().occ, nodes.size());
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;

    final Expr input = arg(0);
    if(mode == Simplify.COUNT) {
      // count(db:node-id(db:text($x)))  ->  count(db:text($x))
      if(input.ddo()) expr = input;
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * Returns the node value.
   * @param node database node
   * @return node ID
   */
  protected int id(final DBNode node) {
    return node.data().id(node.pre());
  }
}
