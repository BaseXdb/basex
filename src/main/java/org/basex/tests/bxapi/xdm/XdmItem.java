package org.basex.tests.bxapi.xdm;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.tests.bxapi.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery items.
 */
public abstract class XdmItem extends XdmValue {
  /**
   * Returns a new XQuery value.
   * @param val value
   * @return result
   */
  public static XdmItem get(final Item val) {
    return val == null ? null : val instanceof ANode ?
        new XdmNode((ANode) val) : new XdmAtomic(val);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public abstract Item internal();

  /**
   * Checks if the two items are equal, according to XQuery.
   * @param item second item
   * @return result of check
   * @throws XQueryException exception
   */
  public boolean equal(final XdmItem item) {
    try {
      return item != null && internal().eq(null, item.internal());
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  @Override
  public final Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      private boolean more = true;

      @Override
      public boolean hasNext() {
        return more;
      }

      @Override
      public XdmItem next() {
        more = false;
        return XdmItem.this;
      }

      @Override
      public void remove() {
        Util.notexpected();
      }
    };
  }
}
