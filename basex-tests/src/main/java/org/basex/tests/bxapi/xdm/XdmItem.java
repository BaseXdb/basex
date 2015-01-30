package org.basex.tests.bxapi.xdm;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.tests.bxapi.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery items.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class XdmItem extends XdmValue {
  /**
   * Returns a new XQuery value.
   * @param val value
   * @return result
   */
  public static XdmItem get(final Item val) {
    return val instanceof ANode ? new XdmNode((ANode) val) : new XdmAtomic(val);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public abstract Item internal();

  /**
   * Checks if the two items are equal, according to XQuery semantics.
   * @param item second item
   * @return result of check
   * @throws XQueryException exception
   */
  public boolean equal(final XdmItem item) {
    if(item == null) return false;
    final Item it1 = internal(), it2 = item.internal();
    try {
      if(!it1.comparable(it2)) throw diffError(null, it1, it2);
      return it1.eq(it2, null, null, null);
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
        throw Util.notExpected();
      }
    };
  }
}
