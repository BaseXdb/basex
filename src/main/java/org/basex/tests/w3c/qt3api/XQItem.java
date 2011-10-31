package org.basex.tests.w3c.qt3api;

import java.util.Iterator;

import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.util.Util;

/**
 * Wrapper for representing XQuery items.
 */
public abstract class XQItem extends XQValue {
  /**
   * Returns a new XQuery value.
   * @param val value
   * @return result
   */
  public static XQItem get(final Item val) {
    return val == null ? null : val instanceof ANode ?
        new XQNode((ANode) val) : new XQAtomic(val);
  }

  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public abstract Item internal();

  @Override
  public final Iterator<XQItem> iterator() {
    return new Iterator<XQItem>() {
      private boolean more = true;

      @Override
      public boolean hasNext() {
        return more;
      }

      @Override
      public XQItem next() {
        more = false;
        return XQItem.this;
      }

      @Override
      public void remove() {
        Util.notexpected();
      }
    };
  }
}
