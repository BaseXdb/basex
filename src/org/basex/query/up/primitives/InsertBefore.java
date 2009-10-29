package org.basex.query.up.primitives;

//import static org.basex.query.up.UpdateFunctions.*;
import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Insert before primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class InsertBefore extends NodeCopy {  
  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   */
  public InsertBefore(final Nod n, final Iter copy) {
    super(n, copy);
  }
  
  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    
    // source nodes may be empty, thus insert has no effect at all
    if(m == null) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int pos = n.pre;
    // [LK] check if parent null?
    d.insertSeq(pos, d.parent(pos, Nod.kind(node.type)), m);
//    if(!mergeTextNodes(d, pos - 1, pos)) {
//      // the number of inserted nodes equals (m.meta.size - 1) because
//      // the DOC root node of the insertion data set is not inserted
//      final int s = m.meta.size - 1;
//      mergeTextNodes(d, pos - s, pos - s - 1);
//    }
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopy) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTBEFORE;
  }
}
