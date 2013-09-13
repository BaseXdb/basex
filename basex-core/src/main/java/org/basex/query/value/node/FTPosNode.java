package org.basex.query.value.node;

import org.basex.data.*;

/**
 * Database node with full-text positions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTPosNode extends DBNode {
  /** Full-text positions. */
  public final FTPosData ft;

  /**
   * Constructor, called by the sequential variant.
   * @param d data reference
   * @param p pre value
   * @param ftpos full-text positions
   */
  public FTPosNode(final Data d, final int p, final FTPosData ftpos) {
    super(d, p);
    ft = ftpos;
  }
}
