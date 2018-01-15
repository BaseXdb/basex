package org.basex.query.util.list;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes.
 *
 * @author BaseX Team 2005-18, BSD License
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
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ANodeList(final int capacity) {
    super(new ANode[capacity]);
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public ANodeList(final ANode... elements) {
    super(elements);
    size = elements.length;
  }

  /**
   * Returns a {@link Value} representation of all items.
   * @return array
   */
  public Value value() {
    return ValueBuilder.value(list, size, NodeType.NOD);
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
      public Value value(final QueryContext qc) {
        return ANodeList.this.value();
      }

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
    };
  }

  @Override
  protected ANode[] newList(final int s) {
    return new ANode[s];
  }
}
