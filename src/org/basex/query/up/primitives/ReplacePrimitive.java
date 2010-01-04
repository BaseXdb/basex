package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.up.NamePool;
import org.basex.query.util.Err;

/**
 * Replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   */
  public ReplacePrimitive(final Nod n, final NodIter replace) {
    super(n, replace);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final int pre = n.pre + add;
    final Data d = n.data;
    final int par = d.parent(pre, Nod.kind(n.type));
    if(n.type == Type.ATT) {
      d.insertAttr(pre, par, md);
    } else {
      d.insert(pre, par, md);
    }
    d.delete(pre + md.meta.size);
    mergeTexts(d, pre, pre + 1);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPL, node.qname());
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
