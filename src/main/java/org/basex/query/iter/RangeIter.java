package org.basex.query.iter;

import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.RangeSeq;
import org.basex.query.item.Value;

/**
 * Range iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class RangeIter extends Iter {
  /** Minimum value. */
  private final long min;
  /** Maximum value (plus one). */
  private final long max;
  /** Current value. */
  private long pos;

  /**
   * Constructor.
   * @param mn minimum value
   * @param mx minimum value
   */
  public RangeIter(final long mn, final long mx) {
    min = mn;
    max = mx + 1;
    pos = mn - 1;
  }

  @Override
  public Item next() {
    pos++;
    return pos != max ? Itr.get(pos) : null;
  }

  @Override
  public long size() {
    return Math.abs(max - min);
  }

  @Override
  public Item get(final long i) {
    return min + i >= max ? null : Itr.get(min + i);
  }

  @Override
  public Value finish() {
    return new RangeSeq(min, max - min);
  }
}
