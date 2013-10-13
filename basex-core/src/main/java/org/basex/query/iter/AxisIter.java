package org.basex.query.iter;

import java.util.*;

import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Interface for light-weight axis iterators, throwing no exceptions.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for (for-each) loops. Note, however, that
 * using the {@link #next()} method will give you better performance.
 *
 * <b>Important</b>: to improve performance, this iterator may return the same node
 * instance with updated values. If resulting nodes are to be further processed,
 * they need to be finalized via {@link ANode#finish()}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class AxisIter extends NodeIter implements Iterable<ANode> {
  @Override
  public abstract ANode next();

  @Override
  public final Iterator<ANode> iterator() {
    return new Iterator<ANode>() {
      /** Current node. */
      private ANode n;

      @Override
      public boolean hasNext() {
        n = AxisIter.this.next();
        return n != null;
      }

      @Override
      public ANode next() {
        return n;
      }

      @Override
      public void remove() {
        Util.notexpected();
      }
    };
  }
}
