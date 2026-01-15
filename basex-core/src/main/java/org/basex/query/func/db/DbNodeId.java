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
    final long size = nodes.size();

    return new Iter() {
      @Override
      public Itr next() throws QueryException {
        final Item item = qc.next(nodes);
        return item != null ? Itr.get(id(item)) : null;
      }
      @Override
      public Itr get(final long i) throws QueryException {
        return Itr.get(id(nodes.get(i)));
      }
      @Override
      public long size() throws QueryException {
        return size;
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) throws QueryException {
        final IntList ids = new IntList(size);
        addIds(nodes.value(qc, expr), ids);
        return IntSeq.get(ids.finish());
      }
    };
  }

  /**
   * Returns a node ID.
   * @param item item
   * @return node ID
   * @throws QueryException query exception
   */
  private int id(final Item item) throws QueryException {
    return id(toDBNode(item, false));
  }

  /**
   * Returns a node ID.
   * @param node database node
   * @return node ID
   */
  int id(final DBNode node) {
    final int pre = node.pre();
    final Data data = node.data();
    return data.meta.updindex ? data.id(pre) : pre;
  }

  /**
   * Adds the IDs.
   * @param nodes nodes
   * @param ids ID list
   * @throws QueryException query exception
   */
  void addIds(final Value nodes, final IntList ids) throws QueryException {
    for(final Item node : nodes) ids.add(id(node));
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
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    Expr expr = this;

    final Expr input = arg(0);
    if(mode == Simplify.COUNT) {
      // count(db:node-id(db:text($x))) → count(db:text($x))
      if(input.ddo()) expr = input;
    }
    return cc.simplify(this, expr, mode);
  }
}
