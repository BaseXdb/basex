package org.basex.query.value.map;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Map that stores few entries in a single array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQSmallMap extends XQMap {
  /** Maximum number of entries. */
  static final int MAX_SIZE = 4;

  /** Keys and values, in alternating order. */
  private final Value[] entries;

  /**
   * Constructor.
   * @param entries keys and values, in alternating order
   */
  XQSmallMap(final Value[] entries) {
    super(Types.MAP);
    this.entries = entries;
  }

  @Override
  public long structSize() {
    return entries.length / 2;
  }

  @Override
  public Value keys() {
    final int el = entries.length;
    final Item[] keys = new Item[el / 2];
    for(int e = 0; e < el; e += 2) keys[e / 2] = (Item) entries[e];
    return ItemSeq.get(keys, el / 2, ((MapType) type).keyType());
  }

  @Override
  public Item keyAt(final long index) {
    return (Item) entries[(int) index * 2];
  }

  @Override
  public Value valueAt(final long index) {
    return entries[(int) index * 2 + 1];
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    final int i = index(key);
    if(i != -1) return putAt(i, value);

    final int el = entries.length;
    if(el < MAX_SIZE * 2) {
      final Value[] copy = Arrays.copyOf(entries, el + 2);
      copy[el] = key;
      copy[el + 1] = value;
      return new XQSmallMap(copy);
    }
    return trie().put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) {
    final int i = index * 2 + 1;
    if(value == entries[i]) return this;

    final Value[] copy = entries.clone();
    copy[i] = value;
    return new XQSmallMap(copy);
  }

  @Override
  public XQMap remove(final Item key) throws QueryException {
    final int i = index(key);
    if(i == -1) return this;

    final int el = entries.length;
    if(el == 2) return empty();

    final int o = i * 2;
    final Value[] copy = new Value[el - 2];
    Array.copy(entries, o, copy);
    Array.copy(entries, o + 2, el - o - 2, copy, o);
    return new XQSmallMap(copy);
  }

  @Override
  public Item shrink(final QueryContext qc) throws QueryException {
    return rebuild(qc);
  }

  @Override
  public int index(final Item key) throws QueryException {
    final int el = entries.length;
    for(int e = 0; e < el; e += 2) {
      if(key.atomicEqual((Item) entries[e])) return e / 2;
    }
    return -1;
  }
}
