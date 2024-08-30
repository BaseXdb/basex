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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DbNodeId extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).iter(qc);
    if(nodes.valueIter()) return ids(nodes.value(qc, null)).iter();

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
  protected final Expr opt(final CompileContext cc) {
    final Expr nodes = arg(0);
    exprType.assign(seqType(), nodes.seqType().occ, nodes.size());
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
