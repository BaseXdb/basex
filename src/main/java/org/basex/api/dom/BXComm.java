package org.basex.api.dom;

import org.basex.query.item.ANode;
import org.w3c.dom.Comment;

/**
 * DOM - Comment implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BXComm extends BXChar implements Comment {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXComm(final ANode n) {
    super(n);
  }
}
