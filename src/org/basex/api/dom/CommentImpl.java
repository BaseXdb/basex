package org.basex.api.dom;

import org.basex.data.Data;
import org.w3c.dom.Comment;

/**
 * DOM - Comment Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CommentImpl extends CharImpl implements Comment {
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public CommentImpl(final Data d, final int p) {
    super(d, p, Data.COMM);
  }
}
