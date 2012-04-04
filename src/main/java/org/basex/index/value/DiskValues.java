package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.IndexCache.CacheEntry;
import org.basex.io.random.*;
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
public class DiskValues implements Index {
  /** Number of index entries. */
  int size;
  /** ID references. */
  final DataAccess idxr;
  /** ID lists. */
  final DataAccess idxl;
  /** Value type (texts/attributes). */
  final boolean text;
  /** Data reference. */
  final Data data;
  /** Cached tokens. */
  final IndexCache cache = new IndexCache();
  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  final IntMap<byte[]> ctext = new IntMap<byte[]>();

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
  DiskValues(final Data d, final boolean txt, final String pref) throws IOException {
    data = d;
    text = txt;
    idxl = new DataAccess(d.meta.dbfile(pref + 'l'));
    idxr = new DataAccess(d.meta.dbfile(pref + 'r'));
    size = idxl.read4();
  }

  @Override
  public synchronized byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE + SORTED_LIST + NL);
    final long l = idxl.length() + idxr.length();
    tb.add(LI_SIZE + Performance.format(l, true) + NL);
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

    final byte[] key = tok.get();

    final CacheEntry e = cache.get(key);
    if(e != null) return iter(e.size, e.pointer);

    final int ix = get(key);
    if(ix < 0) return IndexIterator.EMPTY;
    final long pos = idxr.read5(ix * 5L);
    return iter(idxl.readNum(pos), idxl.cursor());
  }

  @Override
  public synchronized int count(final IndexToken it) {
    if(it instanceof RangeToken) return idRange((RangeToken) it).size();

    final byte[] key = it.get();
    if(key.length > data.meta.maxlen) return Integer.MAX_VALUE;

    final CacheEntry e = cache.get(key);
    if(e != null) return e.size;

    final int ix = get(key);
    if(ix < 0) return 0;
    // get position in heap file
    final long pos = idxr.read5(ix * 5L);
    // the first heap entry represents the number of hits
    final int nr = idxl.readNum(pos);
    cache.add(key, nr, pos + Num.length(nr));
    return nr;
  }

  @Override
  public EntryIterator entries(final byte[] prefix) {
    final int i = get(prefix);
    return new EntryIterator() {
      int ix = (i < 0 ? -i - 1 : i) - 1;
      int nr;
      @Override
      public synchronized byte[] next() {
        while(++ix < size) {
          final long pos = idxr.read5(ix * 5l);
          nr = idxl.readNum(pos);
          final byte[] key = data.text(idxl.readNum(), text);
          if(!startsWith(key, prefix)) break;
          if(prefix.length != 0) cache.add(key, nr, pos + Num.length(nr));
          return key;
        }
        return null;
      }
      @Override
      public int count() {
        return nr;
      }
    };
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
  IndexIterator iter(final int s, final long ps) {
    final IntList pres = new IntList(s);
    long p = ps;
    for(int l = 0, v = 0; l < s; ++l) {
      v += idxl.readNum(p);
      p = idxl.cursor();
      pres.add(v);
    }
    return iter(pres);
  }

  /**
   * Performs a range query. All index values must be numeric.
   * @param tok index term
   * @return results
   */
  IndexIterator idRange(final RangeToken tok) {
    final double min = tok.min;
    final double max = tok.max;

    // check if min and max are positive integers with the same number of digits
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min
        && token(min).length == len;

    final IntList pres = new IntList();
    for(int l = 0; l < size; ++l) {
      final int ds = idxl.readNum(idxr.read5(l * 5L));
      int pre = idxl.readNum();

      final double v = data.textDbl(pre, text);

      if(v >= min && v <= max) {
        // value is in range
        for(int d = 0; d < ds; ++d) {
          pres.add(pre);
          pre += idxl.readNum();
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
  static IndexIterator iter(final IntList ids) {
    return new IndexIterator() {
      final int s = ids.size();
      int p = -1;

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
   * Gets the first pre value from the id-list at the specified position.
   * @param pos position of the id-list in {@link #idxl}
   * @return pre value
   */
  int firstpre(final long pos) {
    // read the number of ids in the list
    idxl.readNum(pos);
    return idxl.readNum();
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * @param key token to be found
   * @return if the key is found: index of the key else: -(insertion point - 1)
   */
  int get(final byte[] key) {
    return get(key, 0, size - 1);
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * <em>Important:</em> This method has to be called while being in the monitor
   * of this instance, e.g. from a {@code synchronized}
   * method.
   * @param key token to be found
   * @param first begin of the search interval
   * @param last end of the search interval
   * @return if the key is found: index of the key else: -(insertion point - 1)
   */
  int get(final byte[] key, final int first, final int last) {
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
   */
  public void flush() {
    idxl.flush();
    idxr.flush();
  }

  @Override
  public synchronized void close() {
    idxl.close();
    idxr.close();
  }

  /**
   * Add entries to the index.
   * @param m a set of <key, id-list> pairs
   */
  @SuppressWarnings("unused")
  public void index(final TokenObjMap<IntList> m) { }

  /**
   * Delete records from the index.
   * @param m a set of <key, id-list> pairs
   */
  @SuppressWarnings("unused")
  public void delete(final TokenObjMap<IntList> m) { }

  /**
   * Remove record from the index.
   * @param o old record key
   * @param n new record key
   * @param id record id
   */
  @SuppressWarnings("unused")
  public void replace(final byte[] o, final byte[] n, final int id) { }
}
