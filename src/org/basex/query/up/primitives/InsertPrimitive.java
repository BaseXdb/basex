package org.basex.query.up.primitives;

import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Basic class of all insert primitives.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class InsertPrimitive extends NodeCopyPrimitive {
  /** Actual insert location of nodes. */
  public int loc;

  /**
   * Constructor.
   * @param n target node
   * @param copy insertion nodes
   * @param l pre location to insert nodes
   */
  protected InsertPrimitive(final Nod n, final Iter copy, final int l) {
    super(n, copy);
    loc = l;
  }

  @Override
  public int ac() {
    return loc;
  }
}
