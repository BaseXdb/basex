package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.Type;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopy {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param rep replace nodes
   */
  public ReplacePrimitive(final InputInfo ii, final ANode n,
      final NodeCache rep) {
    super(ii, n, rep);
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final int pre = n.pre + add;
    final Data d = n.data;
    final int par = d.parent(pre, ANode.kind(n.type));

    //new
    d.delete(pre);

    if(n.type == Type.ATT) d.insertAttr(pre, par, md);
    else d.insert(pre, par, md);
    if(ANode.kind(n.type) == Data.TEXT) mergeTexts(d, pre, pre + 1);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPL.thrw(input, node);
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

  @Override
  public String toString() {
    return Util.name(this) + "[" + node + ", " + insert + "]";
  }
}
