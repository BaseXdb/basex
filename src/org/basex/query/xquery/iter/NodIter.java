package org.basex.query.xquery.iter;

import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Seq;

/**
 * Simple node Iterator, ignoring duplicates.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodIter extends NodeIter {
  /** Items. */
  public Nod[] list;
  /** Size. */
  public int size;
  /** Iterator. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public NodIter() {
    list = new Nod[1];
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  public NodIter(final Nod[] it, final int s) {
    list = it;
    size = s;
  }

  /**
   * Adds a node.
   * @param n node to be added
   */
  public void add(final Nod n) {
    if(size == list.length) resize();
    list[size++] = n;
  }

  /**
   * Resizes the sequence array.
   */
  private void resize() {
    final Nod[] tmp = new Nod[size << 1];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  @Override
  public Nod next() {
    return ++pos < size ? list[pos] : null;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public Item finish() {
    return Seq.get(list, size);
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
