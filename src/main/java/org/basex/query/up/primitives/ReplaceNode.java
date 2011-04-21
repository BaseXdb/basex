package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
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
public final class ReplaceNode extends NodeCopy {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param rep replace nodes
   */
  public ReplaceNode(final InputInfo ii, final ANode n,
      final NodeCache rep) {
    super(ii, n, rep);
  }

  @Override
  public int apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    int pre = n.pre + add;
    final int par = d.parent(pre, d.kind(pre));

    if(n.type == NodeType.TXT && md.meta.size == 1 && md.kind(0) == Data.TEXT) {
      // overwrite existing text node
      d.replace(pre, Data.TEXT, md.text(0, true));
    } else {
      d.delete(pre);
      if(n.type == NodeType.ATT) d.insertAttr(pre, par, md);
      else if(md != null) d.insert(pre, par, md);
      if(!mergeTexts(d, pre - 1, pre)) {
        pre += md != null ? md.meta.size : 1;
        mergeTexts(d, pre - 1, pre);
      }
    }
    return 0;
  }

  @Override
  public void merge(final Primitive p) throws QueryException {
    UPMULTREPL.thrw(input, node);
  }

  @Override
  public void update(final NamePool pool) {
    if(md == null) return;
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
