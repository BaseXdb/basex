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
 * @author BaseX Team 2005-15, BSD License
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

    // update id lists of existing keys (use sorted map to allow for binary search)
    int index = 0;
    final int sz = size();
    for(final byte[] key : new TokenList(map).sort()) {
      index = get(key, index, sz);
      if(index >= 0) {
        final int[] ids = map.get(key).finish();
        final long off = idxr.read5(index * 5L);
        final int oldSize = idxl.readNum(off);
        final IntList il = new IntList(oldSize + ids.length);
        for(int o = 0, c = 0; o < oldSize; ++o) {
          c += idxl.readNum();
          il.add(c);
        }
        // mark old slot as empty
        free.add((int) (idxl.cursor() - off), off);
        // write new ids
        writeIds(key, il.add(ids), index++);
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
        writeIdOffset(newIndex--, off, ctext.put(oldIndex--, null));
      }
      // add the new key and its ids
      writeIds(key, map.get(key), newIndex--);
    }
    size(sz + ns);
  }

  @Override
  public synchronized void delete(final TokenObjMap<IntList> map) {
    // create a list of the indexes of the keys which should be completely deleted
    final IntList il = new IntList();
    int p = 0;
    final int sz = size();
    // add keys in a sorted order (speeds up binary search)
    for(final byte[] key : new TokenList(map).sort()) {
      p = get(key, p, sz);
      if(p < 0) throw Util.notExpected("Key does not exist: '%'", key);
      if(deleteIds(p, key, map.get(key).sort().finish())) il.add(p);
      p++;
    }
    deleteKeys(il);
  }

  /**
   * Removes record ids from the index.
   * @param index index of the key
   * @param key record key
   * @param ids list of record ids to delete
   * @return {@code true} if list was completely deleted
   */
  private boolean deleteIds(final int index, final byte[] key, final int... ids) {
    final long off = idxr.read5(index * 5L);

    // read each id from the list and skip the ones that should be deleted
    final int oldSize = idxl.readNum(off), delSize = ids.length, newSize = oldSize - delSize;
    final IntList newIds = new IntList(newSize);
    for(int o = 0, d = 0, currId = 0; o < oldSize; o++) {
      currId += idxl.readNum();
      if(d < delSize && currId == ids[d]) d++;
      else newIds.add(currId);
    }

    // remove old ids
    free.add((int) (idxl.cursor() - off), off);

    // delete cached index entry if no ids remain
    if(newSize == 0) {
      cache.delete(key);
      return true;
    }

    // write new ids
    writeIds(key, newIds, index);
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
    System.arraycopy(tmp, oldIndex * 5, tmp, newIndex * 5, 5);
    ctext.put(newIndex, ctext.put(oldIndex, null));
  }

  /**
   * Writes a new ID list.
   * @param key key
   * @param ids id list
   * @param index index in reference file
   */
  private void writeIds(final byte[] key, final IntList ids, final int index) {
    // compute compressed size of distance list
    final int[] dists = distances(ids);
    final int sz = dists.length;
    int bytes = Num.length(sz);
    for(final int id : dists) bytes += Num.length(id);

    // choose new insertion position (append at the end if no slot is found)
    final long offset = free.get(bytes, idxl.length());

    // write new id values
    writeIdOffset(index, offset, key);
    idxl.writeNums(offset, dists);

    // update the cache entry
    cache.add(key, sz, offset + Num.length(sz));
  }

  /**
   * Updates an ID reference.
   * @param index index in reference file
   * @param offset offset of ID list
   * @param key key
   */
  private void writeIdOffset(final int index, final long offset, final byte[] key) {
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
   * Returns a new array which contains the id distances in ascending order.
   * @param ids id list
   * @return differences
   */
  private static int[] distances(final IntList ids) {
    final int[] tmp = ids.sort().finish();
    for(int l = tmp.length - 1; l > 0; --l) tmp[l] -= tmp[l - 1];
    return tmp;
  }

  @Override
  public String toString() {
    return super.toString() + free;
  }
}
