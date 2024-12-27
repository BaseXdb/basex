package org.basex.query.value.map;

import java.util.*;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Immutable list of map keys.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class TrieKeys implements Iterable<Item> {
  /** Key array must be copied before being updated, as it is referenced multiple times. */
  private boolean copy;
  /** Keys. */
  private Item[] keys;
  /** Number of keys. */
  private int size;

  /**
   * Empty constructor.
   */
  TrieKeys() {
    this(new Item[2], 0);
  }

  /**
   * Constructor.
   * @param size number of keys
   * @param keys keys
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
    if(size == keys.length) {
      keys = Array.copy(keys, new Item[Array.newCapacity(size)]);
    }
    final Item[] newKeys;
    if(copy) {
      newKeys = keys.clone();
    } else {
      newKeys = keys;
      copy = true;
    }
    newKeys[size] = key;
    return new TrieKeys(newKeys, size + 1);
  }

  /**
   * Returns all keys.
   * @return keys
   */
  Item[] keys() {
    return Arrays.copyOf(keys, size);
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
        final Integer occ = map.get(key);
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
