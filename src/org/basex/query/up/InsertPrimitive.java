package org.basex.query.up;

import org.basex.data.Data;
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
  /** Inserted nodes are attributes. */
  final boolean a;

  /**
   * Constructor
   * @param n target 
   * @param insert insert nodes
   * @param attr inserted nodes are attributes 
   */
  public InsertPrimitive(Nod n, final MemData insert, final boolean attr) {
    super(n);
    a = attr;
    i = insert;
  }

  @Override
  public void apply() {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    if(a)
      UpdateFunctions.insertAttributes(n.pre + d.attSize(n.pre, d.kind(n.pre)), 
          n.pre, d, i);
    else
      d.insertSeq(n.pre + d.attSize(n.pre, Nod.kind(n.type)), n.pre, i);
  }
}
