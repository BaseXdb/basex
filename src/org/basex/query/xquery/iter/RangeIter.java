package org.basex.query.xquery.iter;

import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;

/**
 * Range Iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class RangeIter extends Iter {
  /** Minimum value. */
  private long min;
  /** Maximum value. */
  private long max;
  /** Current value. */
  private long pos;
  /** Ascending flag. */
  private boolean asc;

  /**
   * Constructor.
   * @param mn minimum value
   * @param mx minimum value
   */
  public RangeIter(final long mn, final long mx) {
    min = mn;
    max = mx + 1;
    pos = mn - 1;
    asc = true;
  }

  @Override
  public Item next() {
    pos += asc ? 1 : -1;
    return pos != max ? Itr.get(pos) : null;
  }

  @Override
  public long size() {
    return Math.abs(max - min);
  }

  /**
   * Reverses the iterator.
   */
  public void reverse() {
    pos = max;
    max = min + (asc ? -1 : 1);
    min = pos + (asc ? -1 : 1);
    asc ^= true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(long v = min; v != max; v++) {
      sb.append((v != min ? ", " : "") + v);
      if(sb.length() > 15 && v + 1 != max) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
