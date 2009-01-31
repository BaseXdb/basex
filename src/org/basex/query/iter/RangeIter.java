package org.basex.query.iter;

import org.basex.query.item.Item;
import org.basex.query.item.Itr;

/**
 * Range Iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class RangeIter extends Iter {
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
  public int size() {
    return (int) Math.abs(max - min);
  }

  @Override
  public Item get(final long i) {
    return asc ? min + i >= max ? null : Itr.get(min + i) :
                 min - i <= max ? null : Itr.get(min - i);
  }

  @Override
  public boolean reverse() {
    pos = max;
    max = min + (asc ? -1 : 1);
    min = pos + (asc ? -1 : 1);
    asc ^= true;
    return true;
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
