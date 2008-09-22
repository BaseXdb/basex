package org.basex.api.dom;

import org.basex.query.xquery.item.Nod;
import org.w3c.dom.Comment;

/**
 * DOM - Comment Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BXComment extends BXChar implements Comment {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXComment(final Nod n) {
    super(n);
  }
}
