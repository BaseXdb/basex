package org.basex.gui.view.tree;

import org.basex.data.Data;
import org.basex.util.IntList;

/**
 * Offers an iterator for the children of a node. Could as well be
 * defined as generic child iterator.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ChildIterator {
  /** Data reference. */
  final Data data;
  /** Maximum size. */
  int size;
  /** Current pre value. */
  int pre;
  
  /***
   * Default Constructor.
   * @param d data reference
   * @param p value of directory node
   */
  public ChildIterator(final Data d, final int p) {
    data = d;
    init(p);
  }
  
  /**
   * Initializes the iterator.
   * @param p root pre value
   */
  public void init(final int p) {
    final int k = data.kind(p);
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
  
  /**
   * Returns an array with all pre values. Must be directly called after
   * creating the class instance.
   * @return children array
   */
  public int[] all() {
    final IntList il = new IntList();
    while(more()) il.add(next());
    return il.finish();
  }
}
