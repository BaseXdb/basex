package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * A convenience class for building new maps.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class MapBuilder {
  /** Map. */
  private XQMap map = XQMap.EMPTY;

  /**
   * Adds a key/value pair to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final Item key, final Value value) throws QueryException {
    map = map.put(key, value, null);
    return this;
  }

  /**
   * Adds a key string and a value to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final Value value) throws QueryException {
    return put(Str.get(key), value);
  }

  /**
   * Adds key/value strings to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final String value) throws QueryException {
    return put(Str.get(key), value != null ? Str.get(value) : Empty.VALUE);
  }

  /**
   * Returns and invalidates the resulting map.
   * @return map
   */
  public XQMap finish() {
    final XQMap m = map;
    map = null;
    return m;
  }
}
