package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.w3c.dom.Comment;

/**
 * DOM - Comment implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BXComm extends BXChar implements Comment {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXComm(final Nod n) {
    super(n);
  }
}
