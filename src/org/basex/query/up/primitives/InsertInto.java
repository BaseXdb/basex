package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;

/**
 * Insert into as last primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertInto extends NodeCopy {
  /** Insert into or insert into as last. */
  private boolean last;
  /** Index of most recently added 'insert into' nodes. */
  private int i;

  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param l as last flag
   */
  public InsertInto(final Nod n, final NodIter copy, final boolean l) {
    super(n, copy);
    last = l;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre + d.size(n.pre, Nod.kind(node.type)) + add;
    d.insert(pos, n.pre, m);
    if(!mergeTextNodes(d, pos - 1, pos)) {
      final int s = m.meta.size;
      mergeTextNodes(d, pos + s - 1, pos + s);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    if(((InsertInto) p).last) c.addLast(((NodeCopy) p).c.getFirst());
    else c.add(i++, ((NodeCopy) p).c.getFirst());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTINTO;
  }
}
