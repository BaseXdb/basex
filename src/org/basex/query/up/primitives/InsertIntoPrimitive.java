package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.up.UpdateFunctions;

/**
 * Represents an insert into primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertIntoPrimitive extends NodeCopyPrimitive {

  /**
   * Constructor.
   * @param n target node
   * @param copy copy of nodes to be inserted
   * @param attr copied nodes are attributes
   */
  public InsertIntoPrimitive(final Nod n, final Iter copy, 
      final boolean attr) {
    super(n, copy, attr);
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
    if(a)
      UpdateFunctions.insertAttributes(n.pre + d.attSize(n.pre, d.kind(n.pre)), 
          n.pre, d, m);
    else
      d.insertSeq(n.pre + d.attSize(n.pre, Nod.kind(n.type)), n.pre, m);
  }

  @SuppressWarnings("unused")
  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }

  @Override
  public Type type() {
    return Type.INSERTINTO;
  }
}
