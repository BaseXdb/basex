package org.basex.query.up;

import org.basex.data.MemData;
import org.basex.query.item.Nod;

/**
 * Represents a replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplacePrimitive extends UpdatePrimitive {
  /** Nodes replacing the target. */
  final MemData r;

  /**
   * Constructor.
   * @param n target node
   * @param replace replace nodes
   */
  public ReplacePrimitive(final Nod n, final MemData replace) {
    super(n);
    r = replace;
  }
}
