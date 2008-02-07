package org.basex.query.xquery.iter;

import org.basex.query.xquery.item.Node;

/**
 * Simple node Iterator, ignoring duplicates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class NodIter extends NodeIter {
  /** Items. */
  public Node[] list;
  /** Size. */
  public int size;
  /** Iterator. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public NodIter() {
    list = new Node[1];
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  public NodIter(final Node[] it, final int s) {
    list = it;
    size = s;
  }

  /**
   * Adds a node.
   * @param n node to be added
   */
  public void add(final Node n) {
    if(size == list.length) resize();
    list[size++] = n;
  }

  /**
   * Adds several items.
   * @param i item array
   * @param s number of items to be added
   */
  public void add(final Node[] i, final int s) {
    for(int n = 0; n < s; n++) add(i[n]);
  }

  /**
   * Resizes the sequence array.
   */
  private void resize() {
    final Node[] tmp = new Node[size << 1];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }
  
  @Override
  public Node next() {
    return ++pos < size ? list[pos] : null;
  }
  
  @Override
  public long size() {
    return size;
  }

  @Override
  public void reset() {
    pos = -1;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(int v = 0; v != size; v++) {
      sb.append((v != 0 ? ", " : "") + list[v]);
      if(sb.length() > 15 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
