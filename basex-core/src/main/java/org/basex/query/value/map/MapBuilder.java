package org.basex.query.value.map;

import java.util.*;

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
  /** Inlined keys and values, in alternating order (can be {@code null}). */
  private Value[] entries;
  /** Number of inlined entries. */
  private int size;
  /** Hash map, assigned once the inlined entries overflow (can be {@code null}). */
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

    if(map != null) {
      map = map.build(key, value);
    } else {
      final int i = index(key);
      if(i != -1) {
        entries[i + 1] = value;
      } else if(size < XQSmallMap.MAX_SIZE) {
        // entries are inlined: the final representation is chosen in map()
        final int e = size++ << 1;
        if(entries == null) entries = new Value[2];
        else if(e == entries.length) entries = Arrays.copyOf(entries, e << 1);
        entries[e] = key;
        entries[e + 1] = value;
      } else {
        // too many entries: switch to a hash-based representation
        map = XQHashMap.get(Math.max(capacity, size + 1L), keyType, valueType);
        for(int e = 0; e < size; e++) {
          map = map.build((Item) entries[e << 1], entries[(e << 1) + 1]);
        }
        map = map.build(key, value);
        entries = null;
      }
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
    if(map != null) return map.getOrNull(key);
    final int i = index(key);
    return i != -1 ? entries[i + 1] : null;
  }

  /**
   * Checks if the specified key exists in the map.
   * @param key key to look for
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return map != null ? map.contains(key) : index(key) != -1;
  }

  /**
   * Returns the offset of an inlined entry with the specified key.
   * @param key key to look for
   * @return offset, or {@code -1} if the key does not exist
   * @throws QueryException query exception
   */
  private int index(final Item key) throws QueryException {
    for(int e = 0; e < size; e++) {
      final int i = e << 1;
      if(key.atomicEqual((Item) entries[i])) return i;
    }
    return -1;
  }

  /**
   * Returns the size of the map.
   * @return map size
   */
  public long size() {
    return map != null ? map.structSize() : size;
  }

  /**
   * Returns the built map.
   * @return map
   */
  public XQMap map() {
    if(map == null && size == 0) return XQMap.empty();
    // the representation is chosen from the number of entries, not from the initial capacity
    // the inlined entries are copied: the builder may still be used after this call
    final XQMap mp = map != null ? map :
      size == 1 ? XQMap.get((Item) entries[0], entries[1]) :
      new XQSmallMap(Arrays.copyOf(entries, size << 1));
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
