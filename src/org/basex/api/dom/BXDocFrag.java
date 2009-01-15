package org.basex.api.dom;

import org.basex.query.xquery.item.Nod;
import org.w3c.dom.DocumentFragment;

/**
 * DOM - Document Fragment Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXDocFrag extends BXNode implements DocumentFragment {
  /**
   * Constructor.
   * @param n node reference
   */
  protected BXDocFrag(final Nod n) {
    super(n);
  }

  @Override
  protected int kind() {
    return 7;
  }
}
