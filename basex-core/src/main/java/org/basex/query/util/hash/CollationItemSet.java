package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This set indexes items under the terms of a collation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CollationItemSet implements ItemSet {
  /** Collation keys of strings and untyped items. */
  private final TokenSet strings = new TokenSet();
  /** Other atomic items. */
  private final HashItemSet atomics;
  /** Non-atomic items. */
  private final ItemList others = new ItemList();
  /** All items in insertion order. */
  private final ItemList items = new ItemList();
  /** Deep equality comparisons. */
  private final DeepEqual deep;
  /** Collation. */
  private final Collation coll;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param coll collation
   * @param info input info (can be {@code null})
   */
  CollationItemSet(final Collation coll, final InputInfo info) {
    this.coll = coll;
    this.info = info;
    atomics = new HashItemSet(Mode.DEEP, info);
    deep = new DeepEqual(info, coll, null);
  }

  @Override
  public boolean add(final Item key) throws QueryException {
    final boolean added;
    if(key.type.isStringOrUntyped()) {
      added = strings.add(coll.key(key.string(info), info));
    } else if(key.type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) {
      added = atomics.add(key);
    } else {
      added = !contains(key);
      if(added) others.add(key);
    }
    if(added) items.add(key);
    return added;
  }

  @Override
  public boolean contains(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) return strings.contains(coll.key(key.string(info), info));
    if(key.type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) return atomics.contains(key);
    final int is = others.size();
    for(int i = 0; i < is; i++) {
      if(deep.equal(others.get(i), key)) return true;
    }
    return false;
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }
}
