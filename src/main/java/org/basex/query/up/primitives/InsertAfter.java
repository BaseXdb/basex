package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;
import org.basex.util.InputInfo;

/**
 * Insert after primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class InsertAfter extends NodeCopy {
  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertAfter(final InputInfo ii, final Nod n, final NodIter copy) {
    super(ii, n, copy);
  }

  @Override
  public void apply(final int add) {
    // source nodes may be empty, thus insert has no effect at all
    if(md == null) return;
    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
    final int k = Nod.kind(node.type);
    d.insert(p + d.size(p, k), d.parent(p, k), md);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    insert.add(((NodeCopy) p).insert.get(0));
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTAFTER;
  }
}
