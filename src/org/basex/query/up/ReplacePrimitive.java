package org.basex.query.up;

import static org.basex.query.up.UpdatePrimitive.Type.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Represents a replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends NodeCopyPrimitive {
  /** Replacing nodes are attributes. */
  final boolean a;

  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   * @param attr replacing nodes are attributes
   */
  public ReplacePrimitive(final Nod n, final Iter replace, 
      final boolean attr) {
    super(n, replace);
    a = attr;
  }
  
  @Override
  public void check() throws QueryException {
    super.check();
  }

  @Override
  public void apply() {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int k = Nod.kind(n.type);
    final int par = d.parent(n.pre, d.kind(n.pre));
    if(a)
      UpdateFunctions.insertAttributes(n.pre, par, d, m);
    else d.insertSeq(n.pre + d.size(n.pre, k), par , m);
    d.delete(n.pre);
  }

  @Override
  public Type type() {
    return REPLACENODE;
  }
}
