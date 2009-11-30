package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.NodIter;

/**
 * Insert attribute primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertAttribute extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param copy insertion nods
   */
  public InsertAttribute(final Nod n, final NodIter copy) {
    super(n, copy);
  }

  @Override
  public void apply(final int add) {
    if(m == null) return;
    final DBNode n = (DBNode) node;
    insertAttributes(n.pre + 1, n.pre, n.data, m);
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.INSERTATTR;
  }

  @Override
  public byte[][] addAtt() {
    final byte[][] a = new byte[m.meta.size][];
    for(int i = 0; i < m.meta.size; i++) a[i] = m.attName(i);
    return a;
  }
}
