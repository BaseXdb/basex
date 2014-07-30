package org.basex.index.value;

import static org.basex.data.DataText.*;

import java.io.*;

import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access and update functions to attribute values and text contents stored on
 * disk. The data structure is described in the {@link DiskValuesBuilder} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UpdatableDiskValues extends DiskValues {
  /** Free slots. */
  private final FreeSlots free = new FreeSlots();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param text value type (texts/attributes)
   * @throws IOException I/O Exception
   */
  public UpdatableDiskValues(final Data data, final boolean text) throws IOException {
    super(data, text, text ? DATATXT : DATAATV);
  }

  @Override
  protected int pre(final int id) {
    return data.pre(id);
  }

  @Override
  public synchronized void add(final TokenObjMap<IntList> map) {
    // create a sorted list of the new keys and update the old keys
    final TokenList newKeys = new TokenList();

    // add sorted keys (allows faster binary search)
    int p = 0;
    final int sz = size();
    for(final byte[] key : new TokenList(map).sort(true)) {
      p = get(key, p, sz);
      if(p >= 0) {
        appendIds(key, map.get(key).finish(), p++);
      } else {
        p = -(p + 1);
        newKeys.add(key);
      }
    }

    // insert new keys, starting from the biggest one
    final int ns = newKeys.size();
    for(int j = ns - 1, i = sz - 1, index = sz + j; j >= 0; --j) {
      final byte[] key = newKeys.get(j);
      final int ps = -(1 + get(key, 0, i + 1));
      if(ps < 0) throw Util.notExpected("Key should not exist: '%'", key);

      // create space for new entry
      while(i >= ps) writeRef(index--, i--);
      // add the new key and its ids
      writeList(key, map.get(key), index--);
    }

    size(sz + ns);
  }

  @Override
  public synchronized void delete(final TokenObjMap<IntList> map) {
    // delete ids and create a list of the key positions which should be deleted
    final IntList il = new IntList(map.size());

    // add sorted keys (allows faster binary search)
    int p = 0;
    final int sz = size();
    for(final byte[] key : new TokenList(map).sort(true)) {
      p = get(key, p, sz);
      if(p < 0) throw Util.notExpected("Key does not exist: '%'", key);
      if(deleteIds(p, key, map.get(key).sort().finish())) il.add(p);
      p++;
    }
    // empty should contain sorted keys, since keys were sorted, too
    if(!il.isEmpty()) deleteKeys(il.finish());
  }

  @Override
  public synchronized void replace(final byte[] old, final byte[] key, final int id) {
    // delete the id from the old key
    final int p = get(old);
    if(p >= 0) {
      final int[] tmp = { id };
      if(deleteIds(p, old, tmp)) {
        tmp[0] = p;
        deleteKeys(tmp);
      }
    }
    // add the id to the new key
    insertId(key, id);
  }

  /**
   * Removes record ids from the index.
   * @param index index of the key
   * @param key record key
   * @param ids list of record ids to delete
   * @return {@code true} if list was completely deleted
   */
  private boolean deleteIds(final int index, final byte[] key, final int[] ids) {
    final long pos = idxr.read5(index * 5L);

    // read each id from the list and skip the ones that should be deleted
    final int oldSize = idxl.readNum(pos), delSize = ids.length, newSize = oldSize - delSize;
    final IntList newIds = new IntList(newSize);
    for(int o = 0, d = 0, currId = 0; o < oldSize; o++) {
      currId += idxl.readNum();
      if(d < delSize && currId == ids[d]) d++;
      else newIds.add(currId);
    }

    // remove old ids
    free.add((int) (idxl.cursor() - pos), pos);

    // delete cached key if no ids remain
    if(newSize == 0) {
      cache.delete(key);
      return true;
    }

    // write new ids
    writeList(key, newIds, index);
    return false;
  }

  /**
   * Deletes keys from the index.
   * @param keys list of key positions to delete
   */
  private void deleteKeys(final int[] keys) {
    // shift all keys to the left, skipping the ones which have to be deleted
    int k = 0;
    final int sz = size(), kl = keys.length;
    for(int newIndex = keys[k++], oldIndex = newIndex + 1; oldIndex < sz; ++oldIndex, ++newIndex) {
      if(k < kl && oldIndex == keys[k]) k++;
      writeRef(newIndex, oldIndex);
    }
    // reduce the size of the index
    size(sz - k);
  }

  /**
   * Adds a text entry to the index.
   * @param key text to index
   * @param id id value
   */
  private void insertId(final byte[] key, final int id) {
    int index = get(key);
    if(index < 0) {
      index = -(index + 1);

      // create space for new entry
      final int sz = size();
      for(int i = sz; i > index; --i) writeRef(i, i - 1);

      // add the key and the id
      writeList(key, new IntList(1).add(1), index);

      size(sz + 1);
    } else {
      // add id to the list of ids in the index node
      final long pos = idxr.read5(index * 5L);
      final int num = idxl.readNum(pos);

      final int newSize = num + 1;
      final IntList newIds = new IntList(newSize);
      boolean notadded = true;
      int prevId = 0;
      for(int i = 0; i < num; ++i) {
        int v = idxl.readNum();
        if(notadded && id < prevId + v) {
          // add the new id
          newIds.add(id);
          notadded = false;
          // decrement the difference to the next id
          v -= id - prevId;
          prevId = id;
        }
        newIds.add(id);
        prevId += v;
      }
      if(notadded) newIds.add(id);

      writeList(key, newIds, index);
    }
  }

  /**
   * Adds record ids to an existing index entry.
   * @param key key
   * @param ids sorted list of record ids to add: the first value is the
   * smallest id and all others are only difference to the previous one
   * @param index index of the key
   */
  private void appendIds(final byte[] key, final int[] ids, final int index) {
    // read the old ids
    final long pos = idxr.read5(index * 5L);
    final int oldSize = idxl.readNum(pos);
    final IntList il = new IntList(oldSize + ids.length);
    for(int o = 0; o < oldSize; ++o) il.add(idxl.readNum());

    // remove old and write new ids
    free.add((int) (idxl.cursor() - pos), pos);
    writeList(key, il.add(ids), index);
  }

  /**
   * Writes a new ID list.
   * @param key key
   * @param ids id list
   * @param index index in reference file
   */
  private void writeList(final byte[] key, final IntList ids, final int index) {
    // compute compressed size of distance list
    final int[] dists = distances(ids);
    int bytes = Num.length(dists.length);
    for(final int id : dists) bytes += Num.length(id);

    // choose new insertion position (append at the end if no slot is found)
    final long pos = free.get(bytes, idxl.length());

    // write new id values
    idxl.writeNums(pos, dists);
    writeRef(index, pos, key);

    // update the cache entry
    final int sz = dists.length;
    cache.add(key, sz, pos + Num.length(sz));
  }

  /**
   * Updates an ID reference.
   * @param index index in reference file
   * @param oldIndex old index
   */
  private void writeRef(final int index, final int oldIndex) {
    writeRef(index, idxr.read5(oldIndex * 5L), ctext.get(oldIndex));
    ctext.put(oldIndex, null);
  }

  /**
   * Updates an ID reference.
   * @param index index in reference file
   * @param pos position of ID list
   * @param key key
   */
  private void writeRef(final int index, final long pos, final byte[] key) {
    idxr.write5(index * 5L, pos);
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
   * Returns a new array which contains the id distances in ascending order.
   * @param ids id list
   * @return differences
   */
  private static int[] distances(final IntList ids) {
    final int[] tmp = ids.sort().finish();
    for(int l = tmp.length - 1; l > 0; --l) tmp[l] -= tmp[l - 1];
    return tmp;
  }
}
