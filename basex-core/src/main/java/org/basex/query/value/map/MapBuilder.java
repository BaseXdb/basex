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
  /** Current map implementation. */
  private XQHashMap map;

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
    if(map == null) {
      final Type k = key.type;
      final SeqType v = value.seqType();
      final int c = Array.checkCapacity(capacity);
      final boolean ki = k == AtomType.INTEGER, ks = k == AtomType.STRING;
      final boolean vi = v.eq(SeqType.INTEGER_O), vs = v.eq(SeqType.STRING_O);
      map = ki ? vi ? new XQIntMap(c) : vs ? new XQIntStrMap(c) : new XQIntValueMap(c) :
            ks ? vs ? new XQStrMap(c) : vi ? new XQStrIntMap(c) : new XQStrValueMap(c) :
            new XQItemValueMap(c);
    }
    map = map.build(key, value);
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
    return map != null ? map.size() : 0;
  }

  /**
   * Returns the built map.
   * @return map
   */
  public XQMap map() {
    return map == null ? XQMap.empty() : map.structSize() == 1 ?
      XQMap.singleton(map.keyAt(0), map.valueAt(0)) : map;
  }

  /**
   * Returns the built map and assigns the type of the .
   * @param expr building expression
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
