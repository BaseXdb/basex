package org.basex.index.value;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access and update functions to attribute values and text contents stored on
 * disk. The data structure is described in the {@link DiskValuesBuilder} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdatableDiskValues extends DiskValues {
  /** Free slots. */
  private final FreeSlots free = new FreeSlots();
  /** Pending additions of the current transaction. */
  private ValueCache adds;
  /** Keys emptied by the current transaction, with their positions. */
  private IntObjectMap<byte[]> tombstones = new IntObjectMap<>();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   * @throws IOException I/O exception
   */
  public UpdatableDiskValues(final Data data, final IndexType type) throws IOException {
    super(data, type, fileSuffix(type));
  }

  @Override
  protected int pre(final int id) {
    return data.pre(id);
  }

  @Override
  public synchronized void add(final ValueCache values) {
    // deferred: new keys are inserted once per transaction (see #apply)
    if(adds == null) adds = new ValueCache(type);
    for(final byte[] key : values) {
      final IntList ids = values.ids(key), pos = values.pos(key);
      final int is = ids.size();
      for(int i = 0; i < is; i++) adds.add(key, ids.get(i), pos != null ? pos.get(i) : 0);
    }
  }

  @Override
  public synchronized void delete(final ValueCache values) {
    // entries added by the same transaction have not been written yet: discard them instead
    final ValueCache rest = new ValueCache(type);
    for(final byte[] key : values) {
      final IntList ids = values.ids(key), pos = values.pos(key);
      final int is = ids.size();
      for(int i = 0; i < is; i++) {
        final int id = ids.get(i), ps = pos != null ? pos.get(i) : 0;
        if(adds == null || !adds.remove(key, id, ps)) rest.add(key, id, ps);
      }
    }
    if(!rest.isEmpty()) deleteNow(rest);
  }

  @Override
  public synchronized void finishUpdate() {
    apply();
  }

  @Override
  public synchronized void flush() {
    apply();
    super.flush();
  }

  @Override
  protected byte[] pinned(final int index) {
    return tombstones.get(index);
  }

  /**
   * Removes the emptied keys and inserts the new entries: the key list is rewritten once per
   * transaction instead of once per node, which makes many small changes cheap.
   */
  private void apply() {
    if(!tombstones.isEmpty()) {
      final int[] positions = tombstones.keys();
      Arrays.sort(positions);
      deleteKeys(new IntList(positions));
      tombstones = new IntObjectMap<>();
    }
    if(adds != null) {
      if(!adds.isEmpty()) addNow(adds);
      adds = null;
    }
  }

  /**
   * Adds entries to the index.
   * @param values value cache
   */
  private void addNow(final ValueCache values) {
    // create a sorted list of the new keys and update the old keys
    final TokenList newKeys = new TokenList();

    // update ID lists of keys (in ascending order; speeds up binary search)
    int index = 0;
    final int sz = size();
    for(final byte[] key : values) {
      index = get(key, index, sz);
      if(index >= 0) {
        final IntList ids = values.ids(key), pos = values.pos(key);

        final long off = idxr.read5(index * 5L);
        final int oldSize = idxl.readNum(off), newSize = oldSize + ids.size();
        final IntList newIds = new IntList(newSize);
        final IntList newPos = pos != null ? new IntList(newSize) : null;
        // add existing IDs
        for(int o = 0, c = 0; o < oldSize; o++) {
          c += idxl.readNum();
          newIds.add(c);
          if(newPos != null) newPos.add(idxl.readNum());
        }
        // add new IDs, write new list
        newIds.add(ids.finish());
        if(newPos != null) newPos.add(pos.finish());

        // mark old slot as empty
        free.add((int) (idxl.cursor() - off), off);
        writeIds(key, newIds, newPos, index++);
      } else {
        index = -(index + 1);
        newKeys.add(key);
      }
    }

    // insert new keys in descending order
    final int ns = newKeys.size();
    for(int j = ns - 1, oldIndex = sz - 1, newIndex = sz + j; j >= 0; --j) {
      final byte[] key = newKeys.get(j);
      final int idx = -(1 + get(key, 0, oldIndex + 1));
      if(idx < 0) throw Util.notExpected("Key should not exist: '%'", key);

      // create space for new entry
      while(oldIndex >= idx) {
        final long off = idxr.read5(oldIndex * 5L);
        writeIndex(newIndex--, off, ctext.put(oldIndex--, null));
      }
      // add the new key and its IDs
      writeIds(key, values.ids(key), values.pos(key), newIndex--);
    }
    size(sz + ns);
  }

  /**
   * Deletes entries from the index.
   * @param values value cache
   */
  private void deleteNow(final ValueCache values) {
    int p = 0;
    final int sz = size();
    // update ID lists of keys (in ascending order; speeds up binary search)
    for(final byte[] key : values) {
      p = get(key, p, sz);
      if(p < 0) throw Util.notExpected("Key does not exist: '%'", key);
      // an emptied key is removed at the end of the transaction
      if(deleteIds(p, key, values)) tombstones.put(p, key);
      p++;
    }
  }

  @Override
  protected IntList pres(final int sz, final long offset) {
    return super.pres(sz, offset).sort();
  }

  /**
   * Removes record IDs from the index.
   * @param index index of the key
   * @param key record key
   * @param values value cache
   * @return {@code true} if list was completely deleted
   */
  private boolean deleteIds(final int index, final byte[] key, final ValueCache values) {
    final long off = idxr.read5(index * 5L);
    final IntList ids = values.ids(key).sort();
    final boolean pos = values.pos(key) != null;

    // read each ID from the list and skip the ones that should be deleted
    final int oldSize = idxl.readNum(off), delSize = ids.size(), newSize = oldSize - delSize;
    final IntList newIds = new IntList(newSize), newPos = pos ? new IntList(newSize) : null;
    for(int o = 0, d = 0, currId = 0; o < oldSize; o++) {
      currId += idxl.readNum();
      final int currPos = pos ? idxl.readNum() : 0;
      if(d < delSize && currId == ids.get(d)) {
        d++;
      } else {
        newIds.add(currId);
        if(newPos != null) newPos.add(currPos);
      }
    }

    // no IDs remain: the entry stays in place with an empty list until the end of the
    // transaction, so that lookups passing its position see a consistent state
    if(newSize == 0) {
      idxl.cursor(off);
      idxl.writeNum(0);
      cache.delete(key);
      return true;
    }

    // remove old IDs, write new IDs
    free.add((int) (idxl.cursor() - off), off);
    writeIds(key, newIds, newPos, index);
    return false;
  }

  /**
   * Deletes keys from the index.
   * @param keys list of key positions to delete
   */
  private void deleteKeys(final IntList keys) {
    if(keys.isEmpty()) return;

    int sz = size();
    final int kl = keys.size();
    final byte[] tmp = idxr.readBytes(0, sz * 5);
    for(int k = 0, newIndex = keys.get(k++), oldIndex = newIndex + 1; oldIndex < sz; oldIndex++) {
      if(k < kl && oldIndex == keys.get(k)) {
        k++;
      } else {
        copy(tmp, oldIndex, newIndex++);
      }
    }
    sz -= kl;
    size(sz);

    idxr.cursor(0);
    idxr.writeBytes(tmp, 0, sz * 5);
  }

  /**
   * Copies an ID reference entry in the array.
   * @param tmp reference array
   * @param oldIndex old position
   * @param newIndex new position
   */
  private void copy(final byte[] tmp, final int oldIndex, final int newIndex) {
    Array.copy(tmp, oldIndex * 5, 5, tmp, newIndex * 5);
    ctext.put(newIndex, ctext.put(oldIndex, null));
  }

  /**
   * Writes a new ID list.
   * @param key key
   * @param ids ID list
   * @param pos position list
   * @param index index in reference file
   */
  private void writeIds(final byte[] key, final IntList ids, final IntList pos, final int index) {
    // compute compressed size of distance list
    final int[] nums = prepare(ids, pos);

    // compute byte length and choose new insertion position (append at the end if no slot is found)
    int bytes = Num.length(nums.length);
    for(final int num : nums) bytes += Num.length(num);
    final long offset = free.get(bytes, idxl.length());

    // update key index and compressed numbers
    final int sz = ids.size();
    writeIndex(index, offset, key);
    idxl.cursor(offset);
    idxl.writeNum(sz);
    for(final int num : nums) idxl.writeNum(num);

    // update cache entry
    cache.add(key, sz, offset + Num.length(sz));
  }

  /**
   * Updates an index offset.
   * @param index index in reference file
   * @param offset offset of ID list
   * @param key key
   */
  private void writeIndex(final int index, final long offset, final byte[] key) {
    idxr.write5(index * 5L, offset);
    ctext.put(index, key);
  }

  /**
   * Assigns the number of index entries.
   * @param sz number of index entries
   */
  private void size(final int sz) {
    size.set(sz);
    idxl.write4(0, sz);
  }

  /**
   * Returns a new array which contains ID distances in ascending order, optionally interspersed
   * with token positions.
   * @param ids ID list
   * @param pos position list (can be {@code null})
   * @return differences
   */
  private static int[] prepare(final IntList ids, final IntList pos) {
    final int is = ids.size();
    final IntList result = new IntList(pos == null ? is : (long) is << 1);
    int[] order = null;
    if(pos == null) {
      // no token index: simple sort
      ids.sort();
    } else {
      // tokenization: create array with offsets to ordered values
      order = ids.createOrder(true);
    }

    int lastId = 0;
    for(int i = 0; i < is; i++) {
      final int id = ids.get(i);
      result.add(id - lastId);
      lastId = id;
      if(pos != null) result.add(pos.get(order[i]));
    }
    return result.finish();
  }

  @Override
  public String toString() {
    return super.toString() + free;
  }
}
