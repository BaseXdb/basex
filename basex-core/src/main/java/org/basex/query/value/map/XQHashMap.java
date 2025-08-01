package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Unmodifiable hash map implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class XQHashMap extends XQMap {
  /** Cached immutable variant, for updates. */
  private XQMap trie;

  /**
   * Constructor.
   * @param type map type
   */
  XQHashMap(final Type type) {
    super(type);
  }

  @Override
  public abstract long structSize();

  @Override
  public abstract Value getOrNull(Item key) throws QueryException;

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    return trie().put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) throws QueryException {
    return trie().putAt(index, value);
  }

  /**
   * Replaces the value at the specified position. Called when shrinking data.
   * @param index map index (starting with 0, must be valid)
   * @param value value
   */
  @SuppressWarnings("unused")
  void valueAt(final int index, final Value value) {
    throw Util.notExpected();
  }

  @Override
  public final XQMap remove(final Item key) throws QueryException {
    return getOrNull(key) == null ? this : trie().remove(key);
  }

  @Override
  public final void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 0; i < is; i++) func.accept(keyAt(i), valueAt(i));
  }

  @Override
  public final boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final long is = structSize();
    for(int i = 0; i < is; i++) {
      if(!func.test(keyAt(i), valueAt(i))) return false;
    }
    return true;
  }

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
   * Shrinks the map values.
   * @param qc query exception
   * @throws QueryException query exception
   */
  final void shrinkValues(final QueryContext qc) throws QueryException {
    final long is = structSize();
    for(int i = 0; i < is; i++) valueAt(i, valueAt(i).shrink(qc));
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
      for(int i = 0; i < is; i++) mp = mp.put(keyAt(i), valueAt(i));
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
  static byte[] toStr(final Value value) throws QueryException {
    if(value.seqType().eq(SeqType.STRING_O)) {
      return ((AStr) value).string(null);
    }
    return null;
  }

  /**
   * Tries to convert the value to an untyped atomic.
   * @param value value
   * @return token or {@code null}
   */
  static byte[] toAtm(final Value value) {
    if(value.seqType().eq(SeqType.UNTYPED_ATOMIC_O)) {
      return ((Atm) value).string(null);
    }
    return null;
  }

  /**
   * Tries to convert the value to an integer (excluding {@link Integer#MIN_VALUE}).
   * @param value value
   * @return integer or {@link Integer#MIN_VALUE}
   */
  static int toInt(final Value value) {
    return value instanceof final Itr itr ? itr.toInt() : Integer.MIN_VALUE;
  }
}
