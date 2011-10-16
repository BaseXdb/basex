package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexCache;
import org.basex.index.IndexIterator;
import org.basex.index.IndexStats;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.io.random.DataAccess;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.IntMap;
import org.basex.util.hash.TokenObjMap;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * This class provides access to attribute values and text contents stored on
 * disk. The data structure is described in the {@link ValueBuilder} class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DiskValues implements Index {
  /** Number of index entries. */
  private int size;
  /** ID references. */
  private final DataAccess idxr;
  /** ID lists. */
  private final DataAccess idxl;
  /** Value type (texts/attributes). */
  private final boolean text;
  /** Data reference. */
  private final Data data;
  /** Cache tokens. */
  private final IndexCache cache = new IndexCache();
  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final IntMap<byte[]> ctext = new IntMap<byte[]>();

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @throws IOException I/O Exception
   */
  public DiskValues(final Data d, final boolean txt) throws IOException {
    this(d, txt, txt ? DATATXT : DATAATV);
  }

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @param pref file prefix
   * @throws IOException I/O Exception
   */
  DiskValues(final Data d, final boolean txt, final String pref)
      throws IOException {
    data = d;
    text = txt;
    idxl = new DataAccess(d.meta.dbfile(pref + 'l'));
    idxr = new DataAccess(d.meta.dbfile(pref + 'r'));
    size = idxl.read4();
  }

  @Override
  public synchronized byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INDEXSTRUC + TREESTRUC + NL);
    final long l = idxl.length() + idxr.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);
    final IndexStats stats = new IndexStats(data);
    for(int m = 0; m < size; ++m) {
      final long pos = idxr.read5(m * 5L);
      final int oc = idxl.readNum(pos);
      if(stats.adding(oc)) stats.add(data.text(firstpre(pos), text));
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken tok) {
    if(tok instanceof RangeToken) return idRange((RangeToken) tok);

    final int id = cache.id(tok.get());
    if(id > 0) return iter(cache.size(id), cache.pointer(id));

    final int ix = get(tok.get());
    if(ix < 0) return IndexIterator.EMPTY;
    final long pos = idxr.read5(ix * 5L);
    return iter(idxl.readNum(pos), idxl.cursor());
  }

  @Override
  public synchronized int count(final IndexToken it) {
    if(it instanceof RangeToken) return idRange((RangeToken) it).size();
    if(it.get().length > MAXLEN) return Integer.MAX_VALUE;

    final byte[] tok = it.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.size(id);

    final int ix = get(tok);
    if(ix < 0) return 0;
    final long pos = idxr.read5(ix * 5L);
    // the first number is the number of hits:
    final int nr = idxl.readNum(pos);
    cache.add(it.get(), nr, pos + Num.length(nr));
    return nr;
  }

  /**
   * Returns next values. Called by the {@link ValueBuilder}.
   * @return compressed values
   */
  byte[] nextValues() {
    return idxr.cursor() >= idxr.length() ? EMPTY : idxl.readBytes(
        idxr.read5(), idxl.read4());
  }

  /**
   * Iterator method.
   * @param s number of values
   * @param ps offset
   * @return iterator
   */
  private IndexIterator iter(final int s, final long ps) {
    final IntList pres = new IntList(s);
    long p = ps;
    for(int l = 0, v = 0; l < s; ++l) {
      v += idxl.readNum(p);
      p = idxl.cursor();
      pres.add(data.pre(v));
    }
    return iter(pres.sort());
  }

  /**
   * Performs a range query. All index values must be numeric.
   * @param tok index term
   * @return results
   */
  private IndexIterator idRange(final RangeToken tok) {
    final double min = tok.min;
    final double max = tok.max;

    // check if min and max are positive integers with the same number of digits
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min
        && token(min).length == len;

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

  /**
   * Returns an iterator for the specified id list.
   * @param ids id list
   * @return iterator
   */
  private IndexIterator iter(final IntList ids) {
    return new IndexIterator() {
      int p = -1;
      int s = ids.size();

      @Override
      public boolean more() {
        return ++p < s;
      }

      @Override
      public int next() {
        return ids.get(p);
      }

      @Override
      public double score() {
        return -1;
      }

      @Override
      public int size() {
        return s;
      }
    };
  }

  /**
   * Get the pre value of the first non-deleted id from the id-list at the
   * specified position.
   * @param pos position of the id-list in {@link #idxl}
   * @return {@code -1} if the id-list contains only deleted ids
   */
  private int firstpre(final long pos) {
    // read the number of ids in the list
    idxl.readNum(pos);
    return data.pre(idxl.readNum());
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * @param key token to be found
   * @return if the key is found: index of the key else: -(insertion point - 1)
   */
  private int get(final byte[] key) {
    return get(key, 0, size - 1);
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * <p>
   * <em>Important:</em> This method has to be called while being in the monitor
   * of this {@link DiskValues} instance, e.g. from a {@code synchronized}
   * method.
   * @param key token to be found
   * @param first begin of the search interval
   * @param last end of the search interval
   * @return if the key is found: index of the key else: -(insertion point - 1)
   */
  private int get(final byte[] key, final int first, final int last) {
    int l = first, h = last;
    while(l <= h) {
      final int m = l + h >>> 1;
      byte[] txt = ctext.get(m);
      if(txt == null) {
        txt = data.text(firstpre(idxr.read5(m * 5L)), text);
        ctext.add(m, txt);
      }
      final int d = diff(txt, key);
      if(d == 0) return m;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Flushes the buffered data.
   * @throws IOException I/O exception
   */
  public void flush() throws IOException {
    idxl.write4(0, size);
    idxl.flush();
    idxr.flush();
  }

  @Override
  public synchronized void close() throws IOException {
    idxl.close();
    idxr.close();
  }

  /**
   * Add entries to the index.
   * @param m a set of <key, id-list> pairs
   */
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

    // check if key is cached and update the cache entry
    final int cacheid = cache.id(key);
    if(cacheid > 0)
      cache.update(cacheid, ids.length, newpos + Num.length(ids.length));
  }

  /**
   * Delete records from the index.
   * @param m a set of <key, id-list> pairs
   */
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
    if(!empty.empty()) deleteKeys(empty.toArray());
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

    // check if key is cached and update the cache entry
    final int cacheid = cache.id(key);
    if(cacheid > 0)
      cache.update(cacheid, nids.length, pos + Num.length(nids.length));

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

  /**
   * Remove record from the index.
   * @param o old record key
   * @param n new record key
   * @param id record id
   */
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

      // check if key is cached and update the cache entry
      final int cacheid = cache.id(key);
      if(cacheid > 0)
        cache.update(cacheid, ids.length, newpos + Num.length(ids.length));
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
