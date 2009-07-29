package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.w3c.dom.CDATASection;

/**
 * DOM - CData implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BXCData extends BXText implements CDATASection {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXCData(final Nod n) {
    super(n);
  }

  @Override
  protected int kind() {
    return 6;
  }
}
