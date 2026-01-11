package org.basex.query.value.map;

import java.util.*;

import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Immutable list of map keys.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TrieKeys implements Iterable<Item> {
  /** Key array must be copied before being updated, as it is referenced multiple times. */
  private boolean copy;
  /** Keys. */
  private final Item[] keys;
  /** Number of keys. */
  private final int size;

  /**
   * Empty constructor.
   * @param keys keys
   */
  TrieKeys(final Item... keys) {
    this(keys, keys.length);
  }

  /**
   * Constructor.
   * @param keys keys
   * @param size number of keys
   */
  TrieKeys(final Item[] keys, final int size) {
    this.keys = keys;
    this.size = size;
  }

  /**
   * Adds a key.
   * @param key new key
   * @return new list
   */
  TrieKeys add(final Item key) {
    final int sz = size;
    final Item[] ks;
    if(sz == keys.length) {
      ks = Array.copy(keys, new Item[Array.newCapacity(sz)]);
    } else if(copy) {
      ks = keys.clone();
    } else {
      ks = keys;
      copy = true;
    }
    ks[sz] = key;
    return new TrieKeys(ks, sz + 1);
  }

  /**
   * Returns all keys.
   * @return keys
   */
  Value keys() {
    return ItemSeq.get(keys, size, null);
  }

  /**
   * Returns the key at the specified position.
   * @param index map index (starting with 0, must be valid)
   * @return key
   */
  Item keyAt(final int index) {
    return keys[index];
  }

  /**
   * Removes the specified keys from the list.
   * @param list list with keys to be removed
   * @return new list
   */
  TrieKeys remove(final TrieKeys list) {
    // keys may have been removed and added multiple times: count down occurrences
    final Map<Item, Integer> map = new IdentityHashMap<>();
    for(final Item key : list) map.merge(key, 1, Integer::sum);
    final int sz = size - list.size;
    final ItemList tmp = new ItemList(sz);
    for(final Item key : this) {
      if(!map.isEmpty() && map.containsKey(key)) {
        final int occ = map.get(key);
        if(occ == 1) map.remove(key);
        else map.put(key, occ - 1);
      } else {
        tmp.add(key);
      }
    }
    return new TrieKeys(tmp.finish(), sz);
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<>(keys, size);
  }

  @Override
  public String toString() {
    return Arrays.toString(keys);
  }
}
