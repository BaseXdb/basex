package org.basex.query.up;

import org.basex.data.MemData;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Represents an insert primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class InsertPrimitive extends UpdatePrimitive {
  /** Insert nodes copy. */
  final MemData i;

  /**
   * Constructor
   * @param n target 
   * @param insert insert nodes
   */
  public InsertPrimitive(Nod n, final MemData insert) {
    super(n);
    i = insert;
  }

  @Override
  public void apply() {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    n.data.insertSeq(n.pre + n.data.attSize(n.pre, Nod.kind(n.type)), n.pre, i);
  }
}
