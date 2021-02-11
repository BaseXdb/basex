package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Lazy item.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface Lazy {
  /**
   * Indicates if the contents of this item have been cached.
   * @return result of check
   */
  boolean isCached();

  /**
   * Caches the value.
   * @param info input info
   * @throws QueryException query exception
   */
  void cache(InputInfo info) throws QueryException;
}
