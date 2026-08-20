package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A convenience class for building an {@link XQMap}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapBuilder {
  /** Initial capacity. */
  private final long capacity;
  /** Current map implementation (can be {@code null}). */
  private XQHashMap map;
  /** Union of all key types (can be {@code null}). */
  private Type keyType;
  /** Union of all value types (can be {@code null}). */
  private SeqType valueType;

  /**
   * Constructor.
   */
  public MapBuilder() {
    this(ASet.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity initial capacity
   */
  public MapBuilder(final long capacity) {
    this.capacity = capacity;
  }

  /**
   * Adds a key/value pair to the map.
   * @param key key
   * @param value value
   * @return self reference
   * @throws QueryException query exception
   */
  public MapBuilder put(final Item key, final Value value) throws QueryException {
    // refine the map type while entries are added: no additional pass is required
    final Type kt = key.type;
    final SeqType vt = value.seqType();
    if(valueType == null) {
      keyType = kt;
      valueType = vt;
    } else {
      if(kt != keyType) keyType = keyType.union(kt);
      if(!vt.eq(valueType)) valueType = valueType.union(vt);
    }
    // small maps are inlined; larger ones are assigned a hash-based representation
    if(map == null && capacity <= XQSmallMap.MAX_SIZE) {
      map = XQSmallMap.entry(key, value);
    } else {
      if(map == null) map = XQHashMap.get(capacity, kt, vt);
      map = map.build(key, value);
    }
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
    return map != null ? map.getOrNull(key) : null;
  }

  /**
   * Checks if the specified key exists in the map.
   * @param key key to look for
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return map != null && map.contains(key);
  }

  /**
   * Returns the size of the map.
   * @return map size
   */
  public long size() {
    return map != null ? map.structSize() : 0;
  }

  /**
   * Returns the built map.
   * @return map
   */
  public XQMap map() {
    if(map == null) return XQMap.empty();
    final XQMap mp = map.structSize() == 1 ? XQMap.get(map.keyAt(0), map.valueAt(0)) : map;
    mp.refineType(MapType.get(keyType, valueType));
    return mp;
  }

  /**
   * Returns the built map and annotates it with the type of the specified expression.
   * @param expr expression that created the map
   * @return map
   */
  public XQMap map(final Expr expr) {
    final XQMap mp = map();
    mp.refineType(expr);
    return mp;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + map + ']';
  }
}
