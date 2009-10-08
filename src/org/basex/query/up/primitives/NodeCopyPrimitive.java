package org.basex.query.up.primitives;

import java.util.LinkedList;

import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Abstract update primitive which holds a copy of nodes to be inserted i.e..
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class NodeCopyPrimitive extends UpdatePrimitive {
  /** Copy of nodes to be inserted. */
  LinkedList<Iter> c;
  /** Nodes copied contain attributes. */
  final boolean a;

  /**
   * Constructor.
   * @param n target node
   * @param copy node copy
   * @param attr copied nodes are attributes
   */
  protected NodeCopyPrimitive(final Nod n, final Iter copy, 
      final boolean attr) {
    super(n);
    c = new LinkedList<Iter>();
    c.add(copy);
    a = attr;
  }
}
