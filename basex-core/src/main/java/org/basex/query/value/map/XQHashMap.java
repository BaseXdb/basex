package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Unmodifiable hash map implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class XQHashMap extends XQMap {
  /** Cached immutable variant, for updates. */
  private XQMap trie;
  /** Initial capacity. */
  final long capacity;

  /**
   * Constructor.
   * @param capacity initial capacity
   * @param type map type
   */
  XQHashMap(final long capacity, final Type type) {
    super(type);
    this.capacity = capacity;
  }

  @Override
  public abstract long structSize();

  @Override
  abstract Value getInternal(Item key) throws QueryException;

  @Override
  public final XQMap put(final Item key, final Value value) throws QueryException {
    return trie().put(key, value);
  }

  @Override
  public final XQMap remove(final Item key) throws QueryException {
    return trie().remove(key);
  }

  @Override
  public final void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 1; i <= is; i++) func.accept(keyInternal(i), valueInternal(i));
  }

  @Override
  public final boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 1; i <= is; i++) {
      if(!func.test(keyInternal(i), valueInternal(i))) return false;
    }
    return true;
  }

  @Override
  public final BasicIter<Item> keys() {
    return new BasicIter<>(structSize()) {
      @Override
      public Item get(final long i) {
        return keyInternal((int) i + 1);
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return keysInternal();
      }
    };
  }

  /**
   * Returns all keys.
   * @return key
   */
  abstract Value keysInternal();

  /**
   * Returns the key at the specified position.
   * @param pos position (starting with {@code 1})
   * @return key
   */
  abstract Item keyInternal(int pos);

  /**
   * Returns the value at the specified position.
   * @param pos position (starting with {@code 1})
   * @return key
   */
  abstract Value valueInternal(int pos);

  /**
   * Builds the map by adding a new key and value.
   * @param key key to insert
   * @param value value to insert
   * @return map
   * @throws QueryException query exception
   */
  abstract XQHashMap build(Item key, Value value) throws QueryException;

  /**
   * Builds the map by adding keys and values from the old map and a new key and value.
   * @param old old values
   * @return map
   * @throws QueryException query exception
   */
  final XQHashMap build(final XQHashMap old) throws QueryException {
    old.forEach((QueryBiConsumer<Item, Value>) this::build);
    return this;
  }

  /**
   * Transforms the map to an immutable representation.
   * @return map
   * @throws QueryException query exception
   */
  private XQMap trie() throws QueryException {
    if(trie == null) {
      XQMap mp = empty();
      final long is = structSize();
      for(int i = 1; i <= is; i++) {
        mp = mp.put(keyInternal(i), valueInternal(i));
      }
      trie = mp;
    }
    return trie;
  }

  /**
   * Tries to convert the value to a string.
   * @param value value
   * @return token or {@code null}
   * @throws QueryException query exception
   */
  final byte[] toString(final Value value) throws QueryException {
    if(value.seqType().eq(SeqType.STRING_O)) {
      return ((AStr) value).string(null);
    }
    return null;
  }

  /**
   * Tries to convert the value to an integer (excluding {@link Integer#MIN_VALUE}).
   * @param value value
   * @return integer or {@link Integer#MIN_VALUE}
   */
  final int toInt(final Value value) {
    if(value.seqType().eq(SeqType.INTEGER_O)) {
      final long vl = ((ANum) value).itr();
      final int vi = (int) vl;
      if(vi == vl) return vi;
    }
    return Integer.MIN_VALUE;
  }
}
