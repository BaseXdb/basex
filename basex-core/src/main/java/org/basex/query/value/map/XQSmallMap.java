package org.basex.query.value.map;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Unmodifiable map implementation with a small number of inlined entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQSmallMap extends XQHashMap {
  /** Maximum number of entries. */
  static final int MAX_SIZE = 4;

  /** Keys and values, in alternating order. */
  private final Value[] entries;

  /**
   * Constructor.
   * @param entries keys and values, in alternating order
   */
  private XQSmallMap(final Value[] entries) {
    super(Types.MAP);
    this.entries = entries;
  }

  /**
   * Creates a map with a single entry.
   * @param key key
   * @param value value
   * @return map
   */
  static XQSmallMap entry(final Item key, final Value value) {
    return new XQSmallMap(new Value[] { key, value });
  }

  @Override
  public long structSize() {
    return entries.length >> 1;
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    final int e = index(key);
    return e < 0 ? null : entries[e + 1];
  }

  @Override
  public Value keys() {
    final int el = entries.length;
    final Item[] keys = new Item[el >> 1];
    for(int e = 0; e < el; e += 2) keys[e >> 1] = (Item) entries[e];
    return ItemSeq.get(keys, el >> 1, ((MapType) type).keyType());
  }

  @Override
  public Item keyAt(final long index) {
    return (Item) entries[(int) index << 1];
  }

  @Override
  public Value valueAt(final long index) {
    return entries[((int) index << 1) + 1];
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int i = index(key);
    if(i >= 0) {
      final Value[] copy = entries.clone();
      copy[i + 1] = value;
      return new XQSmallMap(copy);
    }
    final int el = entries.length;
    if(el < MAX_SIZE << 1) {
      final Value[] copy = Arrays.copyOf(entries, el + 2);
      copy[el] = key;
      copy[el + 1] = value;
      return new XQSmallMap(copy);
    }

    XQHashMap mp = XQHashMap.get(MAX_SIZE + 1L, keyAt(0).type, valueAt(0).seqType());
    for(int e = 0; e < el; e += 2) mp = mp.build((Item) entries[e], entries[e + 1]);
    return mp.build(key, value);
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    return rebuild(qc);
  }

  /**
   * Returns the offset of the entry with the specified key.
   * @param key key to look for
   * @return offset, or {@code -1} if the key does not exist
   * @throws QueryException query exception
   */
  private int index(final Item key) throws QueryException {
    final int el = entries.length;
    for(int e = 0; e < el; e += 2) {
      if(key.atomicEqual((Item) entries[e])) return e;
    }
    return -1;
  }
}
