package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for indexing and retrieving items in a set.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public interface ItemSet extends Iterable<Item> {
  /**
   * Indexes the specified key and returns the offset of the added key.
   * If the key already exists, a negative offset is returned.
   * @param key key
   * @param ii input info
   * @return offset of added key, negative offset otherwise
   * @throws QueryException query exception
   */
  int add(final Item key, final InputInfo ii) throws QueryException;

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  int size();
}
