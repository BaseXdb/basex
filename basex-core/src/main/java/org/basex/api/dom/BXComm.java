package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.w3c.dom.*;

/**
 * DOM - Comment implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BXComm extends BXChar implements Comment {
  /**
   * Constructor.
   * @param node node reference
   */
  BXComm(final ANode node) {
    super(node);
  }
}
