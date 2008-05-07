package org.basex.query.fs;

import org.basex.data.Data;

/**
 * Offers an iterator for the children of a node. Could be as well be
 * defined as generic child iterator.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 *
 */
public class DirIterator {
  /** Data reference. */
  final Data data;
  /** Maximum size. */
  final int size;
  /** Current pre value. */
  int pre;
  
  /***
   * Default Constructor.
   * @param d data reference
   * @param p value of directory node
   */
  DirIterator(final Data d, final int p) {
    data = d;
    final int k = d.kind(p);
    size = p + data.size(p, k);
    pre = p + data.attSize(p, k);
  }
  
  /**
   * Returns if the node offers more children.
   * @return result of check
   */
  public boolean more() {
    return pre < size;
  }
  
  /**
   * Returns the pre value of the next child.
   * @return next child reference.
   */
  public int next() {
    final int p = pre;
    pre += data.size(pre, data.kind(pre));
    return p;
  }
}
