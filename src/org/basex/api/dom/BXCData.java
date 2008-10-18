package org.basex.api.dom;

import org.basex.query.xquery.item.Nod;
import org.w3c.dom.CDATASection;

/**
 * DOM - CData Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
