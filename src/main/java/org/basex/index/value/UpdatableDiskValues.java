package org.basex.index.value;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access to attribute values and text contents stored on
 * disk. The data structure is described in the {@link ValueBuilder} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class UpdatableDiskValues extends DiskValues {
  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @throws IOException I/O Exception
   */
  public UpdatableDiskValues(final Data d, final boolean txt) throws IOException {
    this(d, txt, txt ? DATATXT : DATAATV);
  }

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @param pref file prefix
   * @throws IOException I/O Exception
   */
  private UpdatableDiskValues(final Data d, final boolean txt,
      final String pref) throws IOException {
    super(d, txt, pref);
  }

  @Override
  protected IndexIterator iter(final int s, final long ps) {
    final IntList pres = new IntList(s);
    long p = ps;
    for(int l = 0, v = 0; l < s; ++l) {
      v += idxl.readNum(p);
      p = idxl.cursor();
      pres.add(data.pre(v));
    }
    return iter(pres.sort());
  }

  @Override
  protected IndexIterator idRange(final StringRange tok) {
    // check if min and max are positive integers with the same number of digits
    final IntList pres = new IntList();
    final int i = get(tok.min);
    for(int l = i < 0 ? -i - 1 : tok.mni ? i : i + 1; l < size; l++) {
      final int ps = idxl.readNum(idxr.read5(l * 5L));
      int id = idxl.readNum();
      final int pre = data.pre(id);

      // value is too large: skip traversal
      final int d = diff(data.text(pre, text), tok.max);
      if(d > 0 || !tok.mxi && d == 0) break;
      // add pre values
      for(int p = 0; p < ps; ++p) {
        pres.add(data.pre(id));
        id += idxl.readNum();
      }
    }
    return iter(pres.sort());
  }

  @Override
  protected IndexIterator idRange(final NumericRange tok) {
    final double min = tok.min;
    final double max = tok.max;

    // check if min and max are positive integers with the same number of digits
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min &&
        token(min).length == len;

    final IntList pres = new IntList();
    for(int l = 0; l < size; ++l) {
      final int ds = idxl.readNum(idxr.read5(l * 5L));
      int id = idxl.readNum();
      final int pre = data.pre(id);

      final double v = data.textDbl(pre, text);
      if(v >= min && v <= max) {
        // value is in range
        for(int d = 0; d < ds; ++d) {
          pres.add(data.pre(id));
          id += idxl.readNum();
        }
      } else if(simple && v > max && data.textLen(pre, text) == len) {
        // if limits are integers, if min, max and current value have the same
        // string length, and if current value is larger than max, test can be
        // skipped, as all remaining values will be bigger
        break;
      }
    }
    return iter(pres.sort());
  }

  @Override
  protected int firstpre(final long pos) {
    return data.pre(super.firstpre(pos));
  }

  @Override
  public void flush() {
    idxl.write4(0, size);
    super.flush();
  }

  @Override
  public void index(final TokenObjMap<IntList> m) {
    final int last = size - 1;

    // create a sorted list of all keys: allows faster binary search
    final TokenList allkeys = new TokenList(m.keys()).sort(true);

    // create a sorted list of the new keys and update the old keys
    final TokenList nkeys = new TokenList(m.size());
    int p = 0;
    for(final byte[] key : allkeys) {
      p = get(key, p, last);
      if(p < 0) {
        p = -(p + 1);
        nkeys.add(key);
      } else {
        appendIds(p, key, diffs(m.get(key)));
      }
    }

    // insert new keys, starting from the biggest one
    for(int j = nkeys.size() - 1, i = last, pos = size + j; j >= 0; --j) {
      final byte[] key = nkeys.get(j);

      final int ins = -(1 + get(key, 0, i));
      if(ins < 0) throw new IllegalStateException("Key should not exist");

      // shift all bigger keys to the right
      while(i >= ins) {
        idxr.write5(pos * 5L, idxr.read5(i * 5L));
        ctext.add(pos--, ctext.get(i--));
      }

      // add the new key and its ids
      idxr.write5(pos * 5L, idxl.appendNums(diffs(m.get(key))));
      ctext.add(pos--, key);
      // [DP] should the entry be added to the cache?
    }

    size += nkeys.size();
  }

  /**
   * Add record ids to an index entry.
   * @param ix index of the key
   * @param key key
   * @param nids sorted list of record ids to add: the first value is the
   * smallest id and all others are only difference to the previous one
   */
  private void appendIds(final int ix, final byte[] key, final int[] nids) {
    final long oldpos = idxr.read5(ix * 5L);
    final int numold = idxl.readNum(oldpos);
    final int[] ids = new int[numold + nids.length];

    // read the old ids
    for(int i = 0; i < numold; ++i) {
      final int v = idxl.readNum();
      nids[0] -= v; // adjust the first new id
      ids[i] = v;
    }

    // append the new ids - they are bigger than the old ones
    System.arraycopy(nids, 0, ids, numold, nids.length);

    final long newpos = idxl.appendNums(ids);
    idxr.write5(ix * 5L, newpos);

    // update the cache entry
    cache.add(key, ids.length, newpos + Num.length(ids.length));
  }

  @Override
  public void delete(final TokenObjMap<IntList> m) {
    // create a sorted list of all keys: allows faster binary search
    final TokenList allkeys = new TokenList(m.keys()).sort(true);

    // delete ids and create a list of the key positions which should be deleted
    final IntList empty = new IntList(m.size());
    int p = 0;
    for(final byte[] key : allkeys) {
      p = get(key, p, size - 1);
      if(p < 0) p = -(p + 1); // should not occur, but anyway
      else if(deleteIds(p, key, m.get(key).sort().toArray()) == 0) empty.add(p);
    }

    // empty should contain sorted keys, since allkeys was sorted, too
    if(!empty.isEmpty()) deleteKeys(empty.toArray());
  }

  /**
   * Remove record ids from the index.
   * @param ix index of the key
   * @param key record key
   * @param ids list of record ids to delete
   * @return number of remaining records
   */
  private int deleteIds(final int ix, final byte[] key, final int[] ids) {
    final long pos = idxr.read5(ix * 5L);
    final int numold = idxl.readNum(pos);

    if(numold == ids.length) {
      // all ids should be deteted: the key itself will be deleted, too
      cache.delete(key);
      return 0;
    }

    // read each id from the list and skip the ones which should be deleted
    // collect remaining values
    final int[] nids = new int[numold - ids.length];
    for(int i = 0, j = 0, cid = 0, pid = 0; i < nids.length;) {
      cid += idxl.readNum();
      if(j < ids.length && ids[j] == cid) ++j;
      else {
        nids[i++] = cid - pid;
        pid = cid;
      }
    }

    idxl.writeNums(pos, nids);

    // update the cache entry
    cache.add(key, nids.length, pos + Num.length(nids.length));

    return nids.length;
  }

  /**
   * Delete keys from the index.
   * @param keys list of key positions to delete
   */
  private void deleteKeys(final int[] keys) {
    // shift all keys to the left, skipping the ones which have to be deleted
    int j = 0;
    for(int pos = keys[j++], i = pos + 1; i < size; ++i) {
      if(j < keys.length && i == keys[j]) ++j;
      else {
        idxr.write5(pos * 5L, idxr.read5(i * 5L));
        ctext.add(pos++, ctext.get(i));
      }
    }
    // reduce the size of the index
    size -= j;
  }

  @Override
  public void replace(final byte[] o, final byte[] n, final int id) {
    // delete the id from the old key
    final int p = get(o);
    if(p >= 0) {
      final int[] tmp = new int[] { id};
      if(deleteIds(p, o, tmp) == 0) {
        // the old key remains empty: delete it
        cache.delete(o);
        tmp[0] = p;
        deleteKeys(tmp);
      }
    }

    // add the id to the new key
    insertId(n, id);
  }

  /**
   * Add a text entry to the index.
   * @param key text to index
   * @param id id value
   */
  private void insertId(final byte[] key, final int id) {
    int ix = get(key);
    if(ix < 0) {
      ix = -(ix + 1);

      // shift all entries with bigger keys to the right
      for(int i = size; i > ix; --i)
        idxr.write5(i * 5L, idxr.read5((i - 1) * 5L));

      // add the key and the id
      idxr.write5(ix * 5L, idxl.appendNums(new int[] { id}));
      ctext.add(ix, key);
      // [DP] should the entry be added to the cache?

      ++size;
    } else {
      // add id to the list of ids in the index node
      final long pos = idxr.read5(ix * 5L);
      final int num = idxl.readNum(pos);

      final int[] ids = new int[num + 1];
      boolean notadded = true;
      int cid = 0;
      for(int i = 0, j = -1; i < num; ++i) {
        int v = idxl.readNum();

        if(notadded && id < cid + v) {
          // add the new id
          ids[++j] = id - cid;
          notadded = false;
          // decrement the difference to the next id
          v -= id - cid;
          cid = id;
        }

        ids[++j] = v;
        cid += v;
      }

      if(notadded) ids[ids.length - 1] = id - cid;

      final long newpos = idxl.appendNums(ids);
      idxr.write5(ix * 5L, newpos);

      // update the cache entry
      cache.add(key, ids.length, newpos + Num.length(ids.length));
    }
  }

  /**
   * Sort and calculate the differences between a list of ids.
   * @param ids id list
   * @return differences
   */
  private static int[] diffs(final IntList ids) {
    final int[] a = ids.sort().toArray();
    for(int l = a.length - 1; l > 0; --l) a[l] -= a[l - 1];
    return a;
  }
}
