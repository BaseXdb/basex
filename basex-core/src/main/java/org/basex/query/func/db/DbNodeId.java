package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbNodeId extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter nodes = arg(0).unwrappedIter(qc);
    final long size = nodes.size();

    return new Iter() {
      @Override
      public Itr next() throws QueryException {
        final Item item = qc.next(nodes);
        return item != null ? Itr.get(id(item, qc)) : null;
      }
      @Override
      public Itr get(final long i) throws QueryException {
        return Itr.get(id(nodes.get(i), qc));
      }
      @Override
      public long size() {
        return size;
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) throws QueryException {
        final ValueBuilder vb = new ValueBuilder(q, size);
        addIds(nodes.value(qc, expr), vb, qc);
        return vb.value(BasicType.INTEGER);
      }
    };
  }

  /**
   * Returns a node ID.
   * @param item item
   * @param qc query context
   * @return node ID
   * @throws QueryException query exception
   */
  private int id(final Item item, final QueryContext qc) throws QueryException {
    return id(toDBNode(item, qc));
  }

  /**
   * Returns a node ID.
   * @param node database node
   * @return node ID
   */
  int id(final DBNode node) {
    return node.data().id(node.pre());
  }

  /**
   * Adds the IDs.
   * @param nodes nodes
   * @param vb value builder
   * @param qc query context
   * @throws QueryException query exception
   */
  void addIds(final Value nodes, final ValueBuilder vb, final QueryContext qc)
      throws QueryException {
    for(final Item node : nodes) vb.add(id(node, qc));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr nodes = arg(0);
    exprType.assign(seqType(), nodes.seqType().occ, nodes.size());
    return this;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    Expr expr = this;

    final Expr input = arg(0);
    if(mode.oneOf(Simplify.COUNT, Simplify.EXISTENCE)) {
      // count(db:node-id(db:text($x))) → count(db:text($x))
      if(input.ddo()) expr = input;
    }
    return cc.simplify(this, expr, mode);
  }
}
