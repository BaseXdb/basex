package org.basex.query.util.list;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ANodeList extends ObjectList<ANode, ANodeList> {
  /**
   * Constructor.
   */
  public ANodeList() {
    this(1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public ANodeList(final long capacity) {
    super(new ANode[Array.checkCapacity(capacity)]);
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicNodeIter iter() {
    return new BasicNodeIter() {
      int pos;

      @Override
      public long size() {
        return size;
      }
      @Override
      public ANode next() {
        return pos < size ? list[pos++] : null;
      }
      @Override
      public ANode get(final long i) {
        return list[(int) i];
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(list, size, NodeType.NODE.refine(expr));
      }
    };
  }

  /**
   * Invalidates all entries that are not referenced in the list.
   * @return the iterator
   */
  public ANodeList clean() {
    Arrays.fill(list, size, list.length, null);
    return this;
  }

  @Override
  public boolean equals(final ANode node1, final ANode node2) {
    return node1.is(node2);
  }

  @Override
  protected ANode[] newArray(final int s) {
    return new ANode[s];
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof ANodeList && super.equals(obj);
  }
}
