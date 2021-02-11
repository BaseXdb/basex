package org.basex.query.util.collation;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This set indexes items under the terms of a collation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CollationItemSet implements ItemSet {
  /** Items. */
  private final ItemList items = new ItemList();
  /** Collation. */
  private final Collation coll;

  /**
   * Constructor.
   * @param coll collation
   */
  public CollationItemSet(final Collation coll) {
    this.coll = coll;
  }

  @Override
  public boolean add(final Item item, final InputInfo ii) throws QueryException {
    final int is = items.size();
    for(int id = 0; id < is; id++) {
      if(items.get(id).equiv(item, coll, ii)) return false;
    }
    items.add(item);
    return true;
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }
}
