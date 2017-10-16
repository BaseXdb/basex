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
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access to attribute values and text contents stored on disk.
 * The data structure is described in the {@link DiskValuesBuilder} class.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class DiskValues extends ValueIndex {
  /** ID references. */
  final DataAccess idxr;
  /** ID lists. */
  final DataAccess idxl;
  /** Cached index entries: mapping between keys and index entries. */
  final IndexCache cache = new IndexCache();
  /** Cached texts: mapping between key positions in the reference file, and the indexed texts. */
  final IntObjMap<byte[]> ctext = new IntObjMap<>();
  /** Number of current index entries. */
  final AtomicInteger size = new AtomicInteger();

  /** Synchronization object. */
  private final Object monitor = new Object();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   * @throws IOException I/O Exception
   */
  public DiskValues(final Data data, final IndexType type)
      throws IOException {
    this(data, type, fileSuffix(type));
  }

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   * @param pref file prefix
   * @throws IOException I/O Exception
   */
  DiskValues(final Data data, final IndexType type, final String pref) throws IOException {
    super(data, type);
    idxl = new DataAccess(data.meta.dbfile(pref + 'l'));
    idxr = new DataAccess(data.meta.dbfile(pref + 'r'));
    size.set(idxl.read4());
  }

  @Override
  public final byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE).add(SORTED_LIST).add(NL);
    tb.add(LI_NAMES).add(data.meta.names(type)).add(NL);

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    synchronized(monitor) {
      final long l = idxl.length() + idxr.length();
      tb.add(LI_SIZE).add(Performance.format(l)).add(NL);
      final int entries = size();
      for(int index = 0; index < entries; index++) {
        final long pos = idxr.read5(index * 5L);
        final int count = idxl.readNum(pos);
        if(stats.adding(count)) stats.add(key(idxl.readNum()), count);
      }
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public final int size() {
    return size.get();
  }

  @Override
  public final IndexCosts costs(final IndexToken it) {
    return IndexCosts.get(
      it instanceof StringRange ? Math.max(1, data.meta.size / 10) :
      it instanceof NumericRange ? Math.max(1, data.meta.size / 3) :
      entry(it.get()).size);
  }

  @Override
  public final IndexIterator iter(final IndexToken it) {
    if(it instanceof StringRange) return idRange((StringRange) it);
    if(it instanceof NumericRange) return idRange((NumericRange) it);
    final IndexEntry ie = entry(it.get());
    return iter(ie.size, ie.offset);
  }

  @Override
  public final boolean drop() {
    return data.meta.drop(fileSuffix(type) + '.');
  }

  @Override
  public final void close() {
    synchronized(monitor) {
      idxl.close();
      idxr.close();
    }
  }

  @Override
  public void add(final ValueCache vc) {
    throw Util.notExpected();
  }

  @Override
  public void delete(final ValueCache vc) {
    throw Util.notExpected();
  }

  @Override
  public final EntryIterator entries(final IndexEntries input) {
    final byte[] key = input.get();
    if(key.length == 0) return allKeys(input.descending);
    if(input.prefix) return keysWithPrefix(key);
    return keysFrom(key, input.descending);
  }

  @Override
  public final void flush() {
    idxl.flush();
    idxr.flush();
  }

  /**
   * Returns the {@code pre} value for the specified id.
   * @param id id value
   * @return pre value
   */
  protected int pre(final int id) {
    return id;
  }

  /**
   * Binary search for key in the {@code idxr} reference file.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key token to be found
   * @return index of the key, or (-(insertion point) - 1)
   */
  protected final int get(final byte[] key) {
    return get(key, 0, size());
  }

  /**
   * Binary search for key in the {@code #idxr} reference file.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key token to be found
   * @param first begin of the search interval (inclusive)
   * @param last end of the search interval (exclusive)
   * @return index of the key, or (-(insertion point) - 1)
   */
  protected final int get(final byte[] key, final int first, final int last) {
    int l = first, h = last - 1;
    synchronized(monitor) {
      while(l <= h) {
        final int m = l + h >>> 1;
        final byte[] txt = indexEntry(m).key;
        final int d = diff(txt, key);
        if(d == 0) return m;
        if(d < 0) l = m + 1;
        else h = m - 1;
      }
    }
    return -(l + 1);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns an index entry.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key key to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] key) {
    final IndexEntry entry = cache.get(key);
    if(entry != null) return entry;

    final long index = get(key);
    if(index < 0) return new IndexEntry(key, 0, 0);

    final int count;
    final long offset;

    synchronized(monitor) {
      // get position in heap file
      final long pos = idxr.read5(index * 5L);
      count = idxl.readNum(pos);
      offset = idxl.cursor();
    }

    return cache.add(key, count, offset);
  }

  /**
   * Returns all index entries.
   * @param reverse return in a reverse order
   * @return entries
   */
  private EntryIterator allKeys(final boolean reverse) {
    final int s = size() - 1;
    return reverse ? keysWithinReverse(0, s) : keysWithin(0, s);
  }

  /**
   * Returns all index entries, starting from the specified key.
   * @param key key
   * @param reverse return in a reverse order
   * @return entries
   */
  private EntryIterator keysFrom(final byte[] key, final boolean reverse) {
    final int s = size() - 1;
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
      final int s = size();
      int ix = (i < 0 ? -i - 1 : i) - 1; // -1 in order to use the faster ++ix operator
      int count = -1;

      @Override
      public byte[] next() {
        if(++ix < s) {
          synchronized(monitor) {
            final IndexEntry entry = indexEntry(ix);
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
            final IndexEntry entry = indexEntry(ix);
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
            final IndexEntry entry = indexEntry(ix);
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
   * @return index entry
   */
  private IndexEntry indexEntry(final int index) {
    // try the cache first
    byte[] key = ctext.get(index);
    if(key != null) {
      final IndexEntry entry = cache.get(key);
      if(entry != null) return entry;
    }

    // read text and cache result
    final long pos = idxr.read5(index * 5L);
    final int count = idxl.readNum(pos);
    if(key == null) {
      key = key(idxl.readNum());
      ctext.put(index, key);
    }
    return cache.add(key, count, pos + Num.length(count));
  }

  /**
   * Iterator method.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param sz number of values
   * @param offset offset
   * @return iterator
   */
  private IndexIterator iter(final int sz, final long offset) {
    final IntList pres = new IntList(sz);
    synchronized(monitor) {
      idxl.cursor(offset);
      for(int i = 0, id = 0; i < sz; i++) {
        id += idxl.readNum();
        // pass over token position
        if(type == IndexType.TOKEN) idxl.readNum();
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
      final int entries = size();
      for(int index = i < 0 ? -i - 1 : tok.mni ? i : i + 1; index < entries; index++) {
        final int count = idxl.readNum(idxr.read5(index * 5L));
        int id = idxl.readNum();
        // skip traversal if value is too large
        final int diff = diff(key(id), tok.max);
        if(diff > 0 || !tok.mxi && diff == 0) break;
        // add pre values
        for(int c = 0; c < count; c++) {
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
  private IndexIterator idRange(final NumericRange tok) {
    // check if min and max are positive integers with the same number of digits
    final double min = tok.min, max = tok.max;
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min && token(min).length == len;

    final IntList pres = new IntList();
    synchronized(monitor) {
      final int entries = size();
      final boolean text = type == IndexType.TEXT;
      for(int index = 0; index < entries; ++index) {
        final int count = idxl.readNum(idxr.read5(index * 5L));
        int id = idxl.readNum();
        final int pre = pre(id);

        final double v = data.textDbl(pre, text);
        if(v >= min && v <= max) {
          // value is in range
          for(int c = 0; c < count; c++) {
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
      public boolean more() { return ++p < s; }
      @Override
      public int pre() { return pres.get(p); }
      @Override
      public int size() { return s; }
    };
  }

  /**
   * Returns the specified key, considering tokenization.
   * @param id id of key
   * @return key token
   */
  private byte[] key(final int id) {
    final byte[] text = data.text(pre(id), type == IndexType.TEXT);
    return type == IndexType.TOKEN ? distinctTokens(text)[idxl.readNum()] : text;
  }

  /**
   * Returns a string representation of the index structure.
   * @param all include database contents in the representation. During updates, database lookups
   *        must be avoided, as the data structures will be inconsistent.
   * @return string
   */
  public final String toString(final boolean all) {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(type).add(" INDEX, '").add(data.meta.name).add("':\n");
    final int entries = size();
    for(int index = 0; index < entries; index++) {
      final long pos = idxr.read5(index * 5L);
      final int count = idxl.readNum(pos);
      int id = idxl.readNum();
      tb.add("  ").addInt(index).add(". offset: ").addLong(pos);
      if(all) {
        tb.add(", key: \"").add(key(id)).add('"');
        tb.add(", ids").add("/pres").add(": ").addInt(id).add('/').addInt(pre(id));
      } else {
        tb.add(", ids").add(": ").addInt(id);
      }
      for(int c = 1; c < count; c++) {
        id += idxl.readNum();
        tb.add(",").addInt(id);
        if(all) tb.add('/').addInt(pre(id));
      }
      tb.add("\n");
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return toString(false);
  }

  /**
   * Gets the file suffix for the specified index type.
   * @param type index type
   * @return file suffix
   */
  static String fileSuffix(final IndexType type) {
    return type == IndexType.TOKEN ? DATATOK : type == IndexType.TEXT ? DATATXT : DATAATV;
  }
}
