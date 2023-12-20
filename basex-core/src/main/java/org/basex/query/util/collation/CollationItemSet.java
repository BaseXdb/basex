package org.basex.query.util.collation;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This set indexes items under the terms of a collation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CollationItemSet implements ItemSet {
  /** Items. */
  private final ItemList items = new ItemList();
  /** Deep equality comparisons. */
  private final DeepEqual deep;

  /**
   * Returns a hash item set.
   * @param coll collation (can be {@code null})
   * @param info input info (can be {@code null})
   * @return item set
   */
  public static ItemSet get(final Collation coll, final InputInfo info) {
    return coll == null ? new HashItemSet(false, info) : new CollationItemSet(coll, info);
  }

  /**
   * Constructor.
   * @param coll collation
   * @param info input info (can be {@code null})
   */
  private CollationItemSet(final Collation coll, final InputInfo info) {
    deep = new DeepEqual(info, coll, null);
  }

  @Override
  public boolean add(final Item item) throws QueryException {
    final int is = items.size();
    for(int id = 0; id < is; id++) {
      if(deep.equal(items.get(id), item)) return false;
    }
    items.add(item);
    return true;
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }
}
