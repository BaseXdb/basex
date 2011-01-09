package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.w3c.dom.DocumentFragment;

/**
 * DOM - Document fragment implementation.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class BXDocFrag extends BXNode implements DocumentFragment {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXDocFrag(final Nod n) {
    super(n);
  }

  @Override
  protected int kind() {
    return 7;
  }
}
