package org.basex.query.value.node;

import org.basex.data.*;
import org.basex.query.util.ft.*;

/**
 * Database node with full-text positions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTPosNode extends DBNode {
  /** Full-text positions. */
  public final FTPosData ftpos;

  /**
   * Constructor, called by the sequential variant.
   * @param data data reference
   * @param pre PRE value
   * @param ftpos full-text positions
   */
  public FTPosNode(final Data data, final int pre, final FTPosData ftpos) {
    super(data, pre);
    this.ftpos = ftpos;
  }
}
