package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - CData implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class BXCData extends BXText implements CDATASection {
  /**
   * Constructor.
   * @param node node reference
   */
  BXCData(final ANode node) {
    super(node);
  }

  @Override
  protected int kind() {
    // type not specified in database
    return 6;
  }
}
