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
 * @author Dimitar Popov
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
  public synchronized void flush() {
    idxl.write4(0, size.get());
    super.flush();
  }

  @Override
  public synchronized void add(final TokenObjMap<IntList> map) {
    // create a sorted list of the new keys and update the old keys
    final TokenList newKeys = new TokenList();

    // add sorted keys (allows faster binary search)
    int p = 0;
    final int sz = size.get();
    for(final byte[] key : new TokenList(map).sort(true)) {
      p = get(key, p, sz);
      if(p >= 0) {
        appendIds(key, compact(map.get(key)), p++);
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
      writeList(key, compact(map.get(key)), index--, idxl.length());
    }

    size.set(sz + ns);
  }

  @Override
  public synchronized void delete(final TokenObjMap<IntList> map) {
    // delete ids and create a list of the key positions which should be deleted
    final IntList il = new IntList(map.size());

    // add sorted keys (allows faster binary search)
    int p = 0;
    final int sz = size.get();
    for(final byte[] key : new TokenList(map).sort(true)) {
      p = get(key, p, sz);
      if(p < 0) throw Util.notExpected("Key does not exist: '%'", key);
      if(deleteIds(p, key, map.get(key).sort().finish()) == 0) il.add(p);
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
      if(deleteIds(p, old, tmp) == 0) {
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
   * @return number of remaining records
   */
  private int deleteIds(final int index, final byte[] key, final int[] ids) {
    final long pos = idxr.read5(index * 5L);
    final int oldSize = idxl.readNum(pos);
    free.add(oldSize, pos);

    final int delSize = ids.length;
    final int newSize = oldSize - delSize;
    if(newSize == 0) {
      // all ids should be deleted: the key itself will be deleted, too
      cache.delete(key);
    } else {
      // read each id from the list and skip the ones which should be deleted
      // collect remaining values
      final int[] nids = new int[newSize];
      for(int i = 0, j = 0, cid = 0, pid = 0; i < newSize;) {
        cid += idxl.readNum();
        if(j < delSize && ids[j] == cid) ++j;
        else {
          nids[i++] = cid - pid;
          pid = cid;
        }
      }
      writeList(key, nids, index, pos);
    }
    return newSize;
  }

  /**
   * Deletes keys from the index.
   * @param keys list of key positions to delete
   */
  private void deleteKeys(final int[] keys) {
    // shift all keys to the left, skipping the ones which have to be deleted
    int k = 0;
    final int sz = size.get(), kl = keys.length;
    for(int newIndex = keys[k++], oldIndex = newIndex + 1; oldIndex < sz; ++oldIndex) {
      if(k < kl && oldIndex == keys[k]) {
        ++k;
      } else {
        writeRef(newIndex++, oldIndex);
      }
    }
    // reduce the size of the index
    size.set(sz - k);
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
      final int sz = size.get();
      for(int i = sz; i > index; --i) writeRef(i, i - 1);

      // add the key and the id
      writeList(key, new int[] { id }, index, idxl.length());

      size.set(sz + 1);
    } else {
      // add id to the list of ids in the index node
      final long pos = idxr.read5(index * 5L);
      final int num = idxl.readNum(pos);

      final int newSize = num + 1;
      final int[] nids = new int[newSize];
      boolean notadded = true;
      int cid = 0;
      for(int i = 0, j = -1; i < num; ++i) {
        int v = idxl.readNum();

        if(notadded && id < cid + v) {
          // add the new id
          nids[++j] = id - cid;
          notadded = false;
          // decrement the difference to the next id
          v -= id - cid;
          cid = id;
        }

        nids[++j] = v;
        cid += v;
      }
      if(notadded) nids[newSize - 1] = id - cid;

      writeList(key, nids, index, pos);
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
    final long pos = idxr.read5(index * 5L);
    final int oldSize = idxl.readNum(pos);
    free.add(oldSize, pos);

    // read the old ids
    final IntList il = new IntList(oldSize + ids.length);
    for(int i = 0; i < oldSize; ++i) {
      final int v = idxl.readNum();
      ids[0] -= v; // adjust the first new id
      il.add(v);
    }
    writeList(key, il.add(ids).finish(), index, idxl.length());
  }

  /**
   * Writes a new ID list.
   * @param key key
   * @param ids id list
   * @param index index in reference file
   * @param target target position if no free slot is found
   */
  private void writeList(final byte[] key, final int[] ids, final int index, final long target) {
    // find new insertion position
    final int sz = ids.length;
    final Long ps = free.replace(sz);
    final long pos = ps == null ? target : ps;

    // write new id values
    idxl.writeNums(pos, ids);
    writeRef(index, pos, key);

    // update the cache entry
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
   * Returns a new array which contains the id distances in ascending order.
   * @param ids id list
   * @return differences
   */
  private static int[] compact(final IntList ids) {
    final int[] tmp = ids.sort().finish();
    for(int l = tmp.length - 1; l > 0; --l) tmp[l] -= tmp[l - 1];
    return tmp;
  }
}
