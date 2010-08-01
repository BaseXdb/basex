package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.up.NamePool;

/**
 * Replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /**
   * Constructor.
   * @param u updating expression
   * @param n target node
   * @param replace replace nodes
   */
  public ReplacePrimitive(final ParseExpr u, final Nod n,
      final NodIter replace) {
    super(u, n, replace);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final int pre = n.pre + add;
    final Data d = n.data;
    final int par = d.parent(pre, Nod.kind(n.type));

    //new
    d.delete(pre);

    if(n.type == Type.ATT) d.insertAttr(pre, par, md);
    else d.insert(pre, par, md);
    if(Nod.kind(n.type) == Data.TEXT) mergeTexts(d, pre, pre + 1);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    parent.error(UPMULTREPL, node.qname());
  }

  @Override
  public void update(final NamePool pool) {
    add(pool);
    pool.remove(node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACENODE;
  }
}
