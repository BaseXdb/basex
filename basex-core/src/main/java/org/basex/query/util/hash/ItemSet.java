package org.basex.query.util.hash;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is an interface for indexing and retrieving items in a set.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface ItemSet extends Iterable<Item> {
  /** Comparison modes. */
  enum Mode {
    /** Atomic equality. */ ATOMIC,
    /** Deep equality. */ DEEP,
    /** Equality. */ EQUAL,
  }

  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@code true} if the key did not exist yet and was stored
   * @throws QueryException query exception
   */
  boolean add(Item key) throws QueryException;

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   * @throws QueryException query exception
   */
  boolean contains(Item key) throws QueryException;

  /**
   * Returns a hash item set.
   * @param coll collation (can be {@code null})
   * @param info input info (can be {@code null})
   * @return item set
   */
  static ItemSet get(final Collation coll, final InputInfo info) {
    return coll == null ? new HashItemSet(Mode.DEEP, info) : new CollationItemSet(coll, info);
  }
}
