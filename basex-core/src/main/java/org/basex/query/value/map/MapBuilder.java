package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * A convenience class for building an {@link XQMap}.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class MapBuilder {
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Map. */
  private XQMap map = XQMap.empty();

  /**
   * Constructor.
   */
  public MapBuilder() {
    this(null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public MapBuilder(final InputInfo info) {
    this.info = info;
  }

  /**
   * Adds a key/value pair to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final Item key, final Value value) throws QueryException {
    map = map.put(key, value, info);
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
   * Adds a key string and a value token to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final byte[] value) throws QueryException {
    return put(key, value != null ? Str.get(value) : Empty.VALUE);
  }

  /**
   * Adds key/value strings to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final String value) throws QueryException {
    return put(key, value != null ? Str.get(value) : Empty.VALUE);
  }

  /**
   * Checks if the given key exists.
   * @param key key to look for
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return map.contains(key, info);
  }

  /**
   * Returns the value of a map.
   * @param key key to look for
   * @return value or empty sequence
   * @throws QueryException query exception
   */
  public Value get(final Item key) throws QueryException {
    return map.get(key, info);
  }

  /**
   * Returns the resulting map and invalidates the internal reference.
   * @return map
   */
  public XQMap map() {
    final XQMap m = map;
    map = null;
    return m;
  }
}
