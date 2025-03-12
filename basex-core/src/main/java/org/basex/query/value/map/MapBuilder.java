package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * A convenience class for building an {@link XQMap}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapBuilder {
  /** New map instance. */
  private final XQHashMap instance;

  /**
   * Constructor.
   */
  public MapBuilder() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity initial capacity (will be resized to a power of two)
   */
  public MapBuilder(final long capacity) {
    instance = new XQHashMap(new ItemObjectMap<>(capacity));
  }

  /**
   * Adds a key/value pair to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final Item key, final Value value) throws QueryException {
    instance.map.put(key, value);
    return this;
  }

  /**
   * Adds a key and a value token to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final Item key, final byte[] value) throws QueryException {
    return put(key, Str.get(value));
  }

  /**
   * Adds a key string and a value to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final byte[] key, final Value value) throws QueryException {
    return put(Str.get(key), value);
  }

  /**
   * Adds a key string and a value to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final Value value) throws QueryException {
    return put(Token.token(key), value);
  }

  /**
   * Adds key/value tokens to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final byte[] key, final byte[] value) throws QueryException {
    return put(key, value != null ? Str.get(value) : Empty.VALUE);
  }

  /**
   * Adds a key string and a value token to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final byte[] value) throws QueryException {
    return put(Token.token(key), value);
  }

  /**
   * Adds key/value strings to the map.
   * @param key key
   * @param value value (can be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final String key, final String value) throws QueryException {
    return put(Token.token(key), value != null ? Token.token(value) : null);
  }

  /**
   * Returns the value for the specified key.
   * @param key key to look for
   * @return value, or {@code null} if nothing was found
   * @throws QueryException query exception
   */
  public Value get(final Item key) throws QueryException {
    return instance.map.get(key);
  }

  /**
   * Checks if the specified key exists in the map.
   * @param key key to look for
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return instance.map.contains(key);
  }

  /**
   * Returns the resulting map and invalidates the internal reference.
   * @return map
   */
  public XQMap map() {
    final int size = instance.map.size();
    return size == 0 ? XQMap.empty() : size == 1 ?
      XQMap.singleton(instance.map.key(1), instance.map.value(1)) : instance;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + instance + ']';
  }
}
