package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - CData implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXCData extends BXText implements CDATASection {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXCData(final ANode n) {
    super(n);
  }

  @Override
  protected int kind() {
    // type not specified in database
    return 6;
  }
}
