package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This set indexes items under the terms of a collation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CollationItemSet implements ItemSet {
  /** Items. */
  private final ItemList items = new ItemList();
  /** Deep equality comparisons. */
  private final DeepEqual deep;

  /**
   * Constructor.
   * @param coll collation
   * @param info input info (can be {@code null})
   */
  CollationItemSet(final Collation coll, final InputInfo info) {
    deep = new DeepEqual(info, coll, null);
  }

  @Override
  public boolean add(final Item key) throws QueryException {
    if(contains(key)) return false;
    items.add(key);
    return true;
  }

  @Override
  public boolean contains(final Item key) throws QueryException {
    final int is = items.size();
    for(int id = 0; id < is; id++) {
      if(deep.equal(items.get(id), key)) return true;
    }
    return false;
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }
}
