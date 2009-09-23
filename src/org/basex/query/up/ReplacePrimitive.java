package org.basex.query.up;

import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;

/**
 * Represents a replace primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class ReplacePrimitive extends UpdatePrimitive {
  /** Nodes replacing the target. */
  Iter replaceNodes;

  /**
   * Constructor.
   * @param n target node
   * @param r replace nodes
   */
  public ReplacePrimitive(final Nod n, final Iter r) {
    super(n);
    replaceNodes = r;
  }
}
