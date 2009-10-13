package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
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

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void apply(final int add) throws QueryException {
    if(!(node instanceof DBNode)) return;

    final MemData m = buildDB();
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    insertAttributes(n.pre + d.attSize(n.pre, Nod.kind(node.type)), 
        n.pre, d, m);
  }

  @SuppressWarnings("unused")
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTATTR;
  }
}
