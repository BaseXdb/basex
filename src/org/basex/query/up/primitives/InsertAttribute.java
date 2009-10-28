package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Insert attribute primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends InsertPrimitive {
  /**
   * Constructor.
   * @param n target node
   * @param copy insertion nods
   * @param l actual insert location
   */
  public InsertAttribute(final Nod n, final Iter copy, final int l) {
    super(n, copy, l);
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;

    if(m == null) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    insertAttributes(n.pre + 1, n.pre, d, m);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTATTR;
  }
}
