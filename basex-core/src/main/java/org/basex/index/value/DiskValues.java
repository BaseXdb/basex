package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access to attribute values and text contents stored on
 * disk. The data structure is described in the {@link ValueIndexBuilder} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DiskValues implements Index {
  /** ID references. */
  final DataAccess idxr;
  /** ID lists. */
  final DataAccess idxl;
  /** Value type (texts/attributes). */
  private final boolean text;
  /** Data reference. */
  final Data data;
  /** Cached tokens. */
  final IndexCache cache = new IndexCache();
  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  final IntObjMap<byte[]> ctext = new IntObjMap<byte[]>();

  /** Synchronization object. */
  private final Object monitor = new Object();
  /** Number of current index entries. */
  final AtomicInteger size = new AtomicInteger();

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
    size.set(idxl.read4());
  }

  @Override
  public void init() { }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE + SORTED_LIST + NL);
    final IndexStats stats = new IndexStats(data.meta.options.get(MainOptions.MAXSTAT));

    synchronized(monitor) {
      final long l = idxl.length() + idxr.length();
      tb.add(LI_SIZE + Performance.format(l, true) + NL);
      final int s = size.get();
      for(int m = 0; m < s; ++m) {
        final long pos = idxr.read5(m * 5L);
        final int oc = idxl.readNum(pos);
        if(stats.adding(oc)) stats.add(data.text(pre(idxl.readNum()), text));
      }
    }

    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int costs(final IndexToken it) {
    if(it instanceof StringRange) return idRange((StringRange) it).size();
    if(it instanceof NumericRange) return idRange((NumericRange) it).size();
    final byte[] key = it.get();
    return key.length <= data.meta.maxlen ? entry(key).size : Integer.MAX_VALUE;
  }

  @Override
  public IndexIterator iter(final IndexToken it) {
    if(it instanceof StringRange) return idRange((StringRange) it);
    if(it instanceof NumericRange) return idRange((NumericRange) it);
    final IndexEntry e = entry(it.get());
    return iter(e.size, e.pointer);
  }

  /**
   * Returns a cache entry.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param tok token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] tok) {
    final IndexEntry e = cache.get(tok);
    if(e != null) return e;

    final long p = get(tok);
    if(p < 0) return new IndexEntry(tok, 0, 0);

    final int count;
    final long pointer;

    synchronized(monitor) {
      // get position in heap file
      final long pos = idxr.read5(p * 5L);
      // the first heap entry represents the number of hits
      count = idxl.readNum(pos);
      pointer = idxl.cursor();
    }

    return cache.add(tok, count, pointer);
  }

  @Override
  public EntryIterator entries(final IndexEntries input) {
    final byte[] key = input.get();
    if(key.length == 0) return allKeys(input.descending);
    if(input.prefix) return keysWithPrefix(key);
    return keysFrom(key, input.descending);
  }

  /**
   * Returns all index entries.
   * @param reverse return in a reverse order
   * @return entries
   */
  private EntryIterator allKeys(final boolean reverse) {
    final int s = size.get() - 1;
    return reverse ? keysWithinReverse(0, s) : keysWithin(0, s);
  }

  /**
   * Returns all index entries, starting from the specified key.
   * @param key key
   * @param reverse return in a reverse order
   * @return entries
   */
  private EntryIterator keysFrom(final byte[] key, final boolean reverse) {
    final int s = size.get() - 1;
    int i = get(key);
    if(i < 0) i = -i - 1;
    return reverse ? keysWithinReverse(0, i - 1) : keysWithin(i, s);
  }

  /**
   * Returns all index entries with the given prefix.
   * @param prefix prefix
   * @return entries
   */
  private EntryIterator keysWithPrefix(final byte[] prefix) {
    final int i = get(prefix);
    return new EntryIterator() {
      final int s = size.get();
      int ix = (i < 0 ? -i - 1 : i) - 1; // -1 in order to use the faster ++ix operator
      int count = -1;

      @Override
      public byte[] next() {
        if(++ix < s) {
          synchronized(monitor) {
            final IndexEntry entry = readKeyAt(ix);
            if(startsWith(entry.key, prefix)) {
              count = entry.size;
              return entry.key;
            }
          }
        }
        count = -1;
        return null;
      }

      @Override
      public int count() {
        return count;
      }
    };
  }

  /**
   * Returns all index entries within the given range.
   * @param first first entry to be returned
   * @param last last entry to be returned
   * @return entries
   */
  private EntryIterator keysWithin(final int first, final int last) {
    return new EntryIterator() {
      int ix = first - 1;
      int count = -1;

      @Override
      public byte[] next() {
        if(++ix <= last) {
          synchronized(monitor) {
            final IndexEntry entry = readKeyAt(ix);
            count = entry.size;
            return entry.key;
          }
        }
        count = -1;
        return null;
      }

      @Override
      public int count() {
        return count;
      }
    };
  }

  /**
   * Returns all index entries within the given range in a reverse order.
   * @param first first entry to be returned
   * @param last last entry to be returned
   * @return entries
   */
  private EntryIterator keysWithinReverse(final int first, final int last) {
    return new EntryIterator() {
      int ix = last + 1;
      int count = -1;

      @Override
      public byte[] next() {
        if(--ix >= first) {
          synchronized(monitor) {
            final IndexEntry entry = readKeyAt(ix);
            count = entry.size;
            return entry.key;
          }
        }
        count = -1;
        return null;
      }

      @Override
      public int count() {
        return count;
      }
    };
  }

  /**
   * Read a key at the given position.
   * <p><em>Important:</em> This method is NOT thread-safe, since it is used in loops.</p>
   * @param index key position
   * @return key
   */
  IndexEntry readKeyAt(final int index) {
    // try the cache first
    byte[] key = ctext.get(index);
    if(key != null) {
      final IndexEntry entry = cache.get(key);
      if(entry != null) return entry;
    }

    final long pos = idxr.read5(index * 5L);
    // read and ignore the number of ids in the list
    final int cnt = idxl.readNum(pos);
    if(key == null) {
      final int id = idxl.readNum();
      key = data.text(pre(id), text);
    }

    return cache.add(key, cnt, pos + Num.length(cnt));
  }

  /**
   * Iterator method.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param s number of values
   * @param ps offset
   * @return iterator
   */
  private IndexIterator iter(final int s, final long ps) {
    final IntList pres = new IntList(s);
    long p = ps;
    synchronized(monitor) {
      for(int i = 0, id = 0; i < s; i++) {
        id += idxl.readNum(p);
        p = idxl.cursor();
        pres.add(pre(id));
      }
    }
    return iter(pres.sort());
  }

  /**
   * Performs a string-based range query.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param tok index term
   * @return results
   */
  private IndexIterator idRange(final StringRange tok) {
    // check if min and max are positive integers with the same number of digits
    final IntList pres = new IntList();
    synchronized(monitor) {
      final int i = get(tok.min);
      final int s = size.get();
      for(int l = i < 0 ? -i - 1 : tok.mni ? i : i + 1; l < s; l++) {
        final int ps = idxl.readNum(idxr.read5(l * 5L));
        int id = idxl.readNum();
        final int pre = pre(id);

        // value is too large: skip traversal
        final int d = diff(data.text(pre, text), tok.max);
        if(d > 0 || !tok.mxi && d == 0) break;
        // add pre values
        for(int p = 0; p < ps; ++p) {
          pres.add(pre(id));
          id += idxl.readNum();
        }
      }
    }
    return iter(pres.sort());
  }

  /**
   * Performs a range query. All index values must be numeric.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param tok index term
   * @return results
   */
  final IndexIterator idRange(final NumericRange tok) {
    final double min = tok.min;
    final double max = tok.max;

    // check if min and max are positive integers with the same number of digits
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min &&
        token(min).length == len;

    final IntList pres = new IntList();
    synchronized(monitor) {
      final int s = size.get();
      for(int l = 0; l < s; ++l) {
        final int ds = idxl.readNum(idxr.read5(l * 5L));
        int id = idxl.readNum();
        final int pre = pre(id);

        final double v = data.textDbl(pre, text);
        if(v >= min && v <= max) {
          // value is in range
          for(int d = 0; d < ds; ++d) {
            pres.add(pre(id));
            id += idxl.readNum();
          }
        } else if(simple && v > max && data.textLen(pre, text) == len) {
          // if limits are integers, if min, max and current value have the same
          // string length, and if current value is larger than max, test can be
          // skipped, as all remaining values will be bigger
          break;
        }
      }
    }
    return iter(pres.sort());
  }

  /**
   * Returns an iterator for the specified id list.
   * @param pres pre values
   * @return iterator
   */
  private static IndexIterator iter(final IntList pres) {
    return new IndexIterator() {
      final int s = pres.size();
      int p = -1;

      @Override
      public boolean more() {
        return ++p < s;
      }

      @Override
      public int pre() {
        return pres.get(p);
      }

      @Override
      public int size() {
        return s;
      }
    };
  }

  /**
   * Returns the {@code pre} value for the specified id.
   * @param id id value
   * @return pre value
   */
  int pre(final int id) {
    return id;
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key token to be found
   * @return if the key is found: index of the key else: (-(insertion point) - 1)
   */
  int get(final byte[] key) {
    return get(key, 0, size.get());
  }

  /**
   * Binary search for key in the {@link #idxr}.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key token to be found
   * @param first begin of the search interval (inclusive)
   * @param last end of the search interval (exclusive)
   * @return if the key is found: index of the key else: (-(insertion point) - 1)
   */
  int get(final byte[] key, final int first, final int last) {
    int l = first, h = last - 1;
    synchronized(monitor) {
      while(l <= h) {
        final int m = l + h >>> 1;
        final byte[] txt = readKeyAt(m).key;
        final int d = diff(txt, key);
        if(d == 0) return m;
        if(d < 0) l = m + 1;
        else h = m - 1;
      }
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
  public void close() {
    synchronized(monitor) {
      flush();
      idxl.close();
      idxr.close();
    }
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
