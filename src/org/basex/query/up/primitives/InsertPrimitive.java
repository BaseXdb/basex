package org.basex.query.up.primitives;

import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Basic class of all insert primitives.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
abstract class InsertPrimitive extends NodeCopy {
  /** Actual insert location of nodes. */
  private final int loc;

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
  public final int ac() {
    return loc;
  }
}
