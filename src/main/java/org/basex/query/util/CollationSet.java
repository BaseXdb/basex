package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This set indexes items under the terms of a collation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CollationSet implements ItemSet {
  /** Items. */
  private final ValueBuilder items = new ValueBuilder();
  /** Collation. */
  private final Collation coll;

  /**
   * Constructor.
   * @param cl collation
   */
  public CollationSet(final Collation cl) {
    coll = cl;
  }

  @Override
  public int add(final Item item, final InputInfo ii) throws QueryException {
    final int is = size();
    for(int id = 0; id < is; id++) {
      if(items.get(id).equiv(item, coll, ii)) return -id - 1;
    }
    items.add(item);
    return is + 1;
  }

  @Override
  public int size() {
    return (int) items.size();
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }
}
