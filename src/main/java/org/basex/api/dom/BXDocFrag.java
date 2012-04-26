package org.basex.api.dom;

import org.basex.query.item.*;
import org.w3c.dom.*;

/**
 * DOM - Document fragment implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXDocFrag extends BXNode implements DocumentFragment {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXDocFrag(final ANode n) {
    super(n);
  }

  @Override
  protected int kind() {
    return 7;
  }
}
