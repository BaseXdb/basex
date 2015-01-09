package org.basex.query.util.hash;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is an interface for indexing and retrieving items in a set.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface ItemSet extends Iterable<Item> {
  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @param ii input info
   * @return {@code true} if the key did not exist yet and was stored
   * @throws QueryException query exception
   */
  boolean add(final Item key, final InputInfo ii) throws QueryException;
}
