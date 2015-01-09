package org.basex.query.value.node;

import org.basex.data.*;

/**
 * Database node with full-text positions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FTPosNode extends DBNode {
  /** Full-text positions. */
  public final FTPosData ftpos;

  /**
   * Constructor, called by the sequential variant.
   * @param data data reference
   * @param pre pre value
   * @param ftpos full-text positions
   */
  public FTPosNode(final Data data, final int pre, final FTPosData ftpos) {
    super(data, pre);
    this.ftpos = ftpos;
  }
}
