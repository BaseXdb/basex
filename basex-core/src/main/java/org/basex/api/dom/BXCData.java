package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - CData implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class BXCData extends BXText implements CDATASection {
  /**
   * Constructor.
   * @param node node reference
   */
  BXCData(final XNode node) {
    super(node);
  }

  @Override
  int kind() {
    // type not specified in database
    return 6;
  }
}
