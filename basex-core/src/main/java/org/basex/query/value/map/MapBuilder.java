package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * A convenience class for building new maps.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MapBuilder {
  /** Map. */
  private XQMap map = XQMap.EMPTY;

  /**
   * Adds a key/value pair to the map.
   * @param key key
   * @param value value
   * @throws QueryException query exception
   */
  public void put(final Item key, final Value value) throws QueryException {
    map = map.put(key, value, null);
  }

  /**
   * Adds a key string and a value to the map.
   * @param key key
   * @param value value
   * @throws QueryException query exception
   */
  public void put(final String key, final Value value) throws QueryException {
    put(Str.get(key), value);
  }

  /**
   * Adds key/value strings to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @throws QueryException query exception
   */
  public void put(final String key, final String value) throws QueryException {
    put(Str.get(key), value != null ? Str.get(value) : Empty.VALUE);
  }

  /**
   * Returns the resulting map and invalidates the internal reference.
   * @return map
   */
  public XQMap finish() {
    final XQMap m = map;
    map = null;
    return m;
  }
}
