package org.basex.query.up.primitives;

import static org.basex.query.up.primitives.UpdatePrimitive.Type.*;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.up.UpdateFunctions;

/**
 * Represents an insert primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertPrimitive extends NodeCopyPrimitive {
  /** Inserted nodes are attributes. */
  final boolean a;

  /**
   * Constructor.
   * @param n target 
   * @param insert insert nodes
   * @param attr inserted nodes are attributes 
   */
  public InsertPrimitive(final Nod n, final Iter insert, final boolean attr) {
    super(n, insert);
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
    if(a)
      UpdateFunctions.insertAttributes(n.pre + d.attSize(n.pre, d.kind(n.pre)), 
          n.pre, d, m);
    else
      d.insertSeq(n.pre + d.attSize(n.pre, Nod.kind(n.type)), n.pre, m);
  }

  @Override
  public Type type() {
    return a ? INSERTATTR : INSERTINTO;
  }
  
  @Override
  public void merge(final UpdatePrimitive p) {
    c.add(((NodeCopyPrimitive) p).c.getFirst());
  }
}
