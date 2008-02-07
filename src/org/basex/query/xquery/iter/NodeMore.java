package org.basex.query.xquery.iter;

/**
 * Iterator interface, extending the default iterator with a {@link #more}
 * method.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class NodeMore extends NodeIter {
  /**
   * Checks if more nodes are found.
   * @return temporary node
   */
  public abstract boolean more();
}
