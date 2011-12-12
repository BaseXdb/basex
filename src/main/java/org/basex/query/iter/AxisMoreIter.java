package org.basex.query.iter;

import org.basex.query.item.ANode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Value;

/**
 * Iterator interface, extending the default iterator with a {@link #more}
 * method.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class AxisMoreIter extends AxisIter {
  /** Empty iterator. */
  public static final AxisMoreIter EMPTY = new AxisMoreIter() {
    @Override public boolean more() { return false; }
    @Override public ANode next() { return null; }
    @Override public Item get(final long i) { return null; }
    @Override public long size() { return 0; }
    @Override public boolean reset() { return true; }
    @Override public Value value() { return Empty.SEQ; }
  };

  /**
   * Checks if more nodes are found.
   * @return temporary node
   */
  public abstract boolean more();
}
