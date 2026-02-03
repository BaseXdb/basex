package org.basex.query.iter;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Basic node iterator, throwing no exceptions.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for(for-each) loops. Note, however, that
 * the {@link #next()} method will give you better performance.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class BasicNodeIter extends NodeIter implements Iterable<XNode> {
  /** Empty iterator. */
  public static final BasicNodeIter EMPTY = new BasicNodeIter() {
    @Override
    public XNode next() {
      return null;
    }
    @Override
    public long size() {
      return 0;
    }
    @Override
    public boolean valueIter() {
      return true;
    }
    @Override
    public Empty value(final QueryContext qc, final Expr expr) {
      return Empty.VALUE;
    }
  };

  @Override
  public abstract XNode next();

  @Override
  public final Iterator<XNode> iterator() {
    return new Iterator<>() {
      private XNode node;

      @Override
      public boolean hasNext() {
        final XNode n = BasicNodeIter.this.next();
        node = n;
        return n != null;
      }

      @Override
      public XNode next() {
        return node;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }
}
