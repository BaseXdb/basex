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
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides access to attribute values and text contents stored on disk.
 * The data structure is described in the {@link DiskValuesBuilder} class.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public DiskValues(final Data data, final IndexType type) throws IOException {
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
    idxl = new DataAccess(data.meta.dbFile(pref + 'l'));
    idxr = new DataAccess(data.meta.dbFile(pref + 'r'));
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
  public final IndexCosts costs(final IndexSearch search) {
    return IndexCosts.get(
      search instanceof StringRange ? Math.max(1, data.meta.size / 10) :
      search instanceof NumericRange ? Math.max(1, data.meta.size / 3) :
      entry(search.token()).size);
  }

  @Override
  public final IndexIterator iter(final IndexSearch search) {
    final IntList pres;
    if(search instanceof StringRange) {
      pres = idRange((StringRange) search);
    } else if(search instanceof NumericRange) {
      pres = idRange((NumericRange) search);
    } else {
      final IndexEntry ie = entry(search.token());
      pres = pres(ie.size, ie.offset);
    }

    return new IndexIterator() {
      final int sz = pres.size();
      int c;

      @Override
      public boolean more() {
        return c < sz;
      }

      @Override
      public int pre() {
        return pres.get(c++);
      }

      @Override
      public int size() {
        return sz;
      }
    };
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
  public void add(final ValueCache values) {
    throw Util.notExpected();
  }

  @Override
  public void delete(final ValueCache values) {
    throw Util.notExpected();
  }

  @Override
  public final EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();
    if(token.length == 0) return keys(0, size(), entries.descending);
    if(entries.prefix) return keys(token);

    int i = get(token);
    if(i < 0) i = -i - 1;
    return entries.descending ? keys(0, i, true) : keys(i, size(), false);
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
   * @param token token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] token) {
    final IndexEntry entry = cache.get(token);
    if(entry != null) return entry;

    final long index = get(token);
    if(index < 0) return new IndexEntry(token, 0, 0);

    final int count;
    final long offset;

    synchronized(monitor) {
      // get position in heap file
      final long pos = idxr.read5(index * 5L);
      count = idxl.readNum(pos);
      offset = idxl.cursor();
    }

    return cache.add(token, count, offset);
  }

  /**
   * Returns all index entries with the given prefix.
   * @param prefix prefix
   * @return entries
   */
  private EntryIterator keys(final byte[] prefix) {
    final int first = get(prefix), sz = size();

    return new EntryIterator() {
      int c = first < 0 ? -first - 1 : first;
      IndexEntry entry;

      @Override
      public byte[] next() {
        if(c < sz) {
          synchronized(monitor) {
            entry = indexEntry(c++);
            if(startsWith(entry.key, prefix)) return entry.key;
          }
        }
        return null;
      }

      @Override
      public int count() {
        return entry.size;
      }
    };
  }

  /**
   * Returns all index entries within the given range.
   * @param first first entry to be returned
   * @param last last entry to be returned (exclusive)
   * @param reverse return in reverse order
   * @return entries
   */
  private EntryIterator keys(final int first, final int last, final boolean reverse) {
    final int sz = last - first;
    return new EntryIterator() {
      int c;
      IndexEntry entry;

      @Override
      public byte[] next() {
        return c < sz ? get(c++) : null;
      }

      @Override
      public int count() {
        return entry.size;
      }

      @Override
      public byte[] get(final int i) {
        synchronized(monitor) {
          entry = indexEntry(reverse ? last - i - 1 : first + i);
          return entry.key;
        }
      }

      @Override
      public int size() {
        return sz;
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
   * @return sorted pre values
   */
  protected IntList pres(final int sz, final long offset) {
    final IntList pres = new IntList(sz);
    synchronized(monitor) {
      idxl.cursor(offset);
      for(int i = 0, id = 0; i < sz; i++) {
        id += idxl.readNum();
        // token index: skip position
        if(type == IndexType.TOKEN) idxl.readNum();
        pres.add(pre(id));
      }
    }
    return pres;
  }

  /**
   * Performs a string-based range query.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param tok index term
   * @return results
   */
  private IntList idRange(final StringRange tok) {
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
    return pres.sort();
  }

  /**
   * Performs a range query. All index values must be numeric.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param tok index term
   * @return results
   */
  private IntList idRange(final NumericRange tok) {
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
    return pres.sort();
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
   *        must be avoided, as the data structures will be inconsistent
   * @return string
   */
  public final String toString(final boolean all) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(type).add(" INDEX, '").add(data.meta.name).add("':\n");
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
