package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.w3c.dom.DocumentFragment;

/**
 * DOM - Document fragment implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class BXDocFrag extends BXNode implements DocumentFragment {
  /**
   * Constructor.
   * @param n node reference
   */
  BXDocFrag(final Nod n) {
    super(n);
  }

  @Override
  protected int kind() {
    return 7;
  }
}
