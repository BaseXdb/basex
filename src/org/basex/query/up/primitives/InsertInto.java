package org.basex.query.up.primitives;

import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Represents an insert into primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertInto extends NodeCopy {
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertInto(final Nod n, final Iter copy) {
    super(n, copy);
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre + d.size(n.pre, Nod.kind(node.type));
    n.data.insertSeq(pos, n.pre, m);
    if(!mergeTextNodes(d, pos - 1, pos)) {
      // the number of inserted nodes equals (m.meta.size - 1) because
      // the DOC root node of the insertion data set is not inserted
      final int s = m.meta.size - 1;
      mergeTextNodes(d, pos + s - 1, pos + s);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }
  
  @Override
  public Type type() {
    return Type.INSERTINTO;
  }
}