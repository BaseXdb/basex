package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.w3c.dom.CDATASection;

/**
 * DOM - CData implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class BXCData extends BXText implements CDATASection {
  /**
   * Constructor.
   * @param n node reference
   */
  protected BXCData(final Nod n) {
    super(n);
  }

  @Override
  protected int kind() {
    // type not specified in database
    return 6;
  }
}
