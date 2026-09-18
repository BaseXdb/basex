package org.basex.index.value;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Caches values and IDs for update operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ValueCache implements Iterable<byte[]> {
  /** IDs, indexed by keys. */
  private final TokenObjectMap<IntList> ids = new TokenObjectMap<>();

  /**
   * Caches all texts and IDs in the specified database range.
   * @param pre PRE value
   * @param size size value
   * @param type index type
   * @param data data reference
   */
  ValueCache(final int pre, final int size, final IndexType type, final Data data) {
    this(pres(pre, size), type, data);
  }

  /**
   * Caches texts of the specified PRE values.
   * @param pres PRE values
   * @param type index type
   * @param data data reference
   */
  ValueCache(final IntList pres, final IndexType type, final Data data) {
    final IndexNames in = new IndexNames(type, data);
    final int pl = pres.size();
    for(int p = 0; p < pl; p++) {
      final int pre = pres.get(p);
      if(in.unit(pre)) {
        final int id = data.id(pre);
        ValueIndex.keys(data, type, pre,
            (key, pos) -> ids.computeIfAbsent(key, IntList::new).add(id));
      }
    }
  }

  /**
   * Caches all texts and IDs in the specified database range.
   * @param pre PRE value
   * @param size size value
   * @return list of PRE values
   */
  private static IntList pres(final int pre, final int size) {
    final IntList pres = new IntList(size);
    final int last = pre + size;
    for(int curr = pre; curr < last; curr++) pres.add(curr);
    return pres;
  }

  /**
   * Returns an iterator with all keys, in sorted order.
   * @return keys iterator
   */
  @Override
  public Iterator<byte[]> iterator() {
    return new TokenList(ids).sort().iterator();
  }

  /**
   * Returns the ID list for the specified key.
   * @param key key
   * @return ID list
   */
  IntList ids(final byte[] key) {
    return ids.get(key);
  }
}
