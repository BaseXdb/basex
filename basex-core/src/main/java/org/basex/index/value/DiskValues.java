package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

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
 * This class provides access to attribute values and text contents stored on disk.
 * The data structure is described in the {@link DiskValuesBuilder} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DiskValues extends ValueIndex {
  /** ID references. */
  private final DataAccess idxr;
  /** ID lists. */
  private final DataAccess idxl;
  /** Cached index entries: mapping between keys and index entries. */
  private final IndexCache cache = new IndexCache();
  /** Cached texts: mapping between key positions in the reference file, and the indexed texts. */
  private final IntObjectMap<byte[]> ctext = new IntObjectMap<>();
  /** Number of current index entries. */
  private final int size;

  /** Synchronization object. */
  private final Object monitor = new Object();

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @param type index type
   * @throws IOException I/O exception
   */
  public DiskValues(final Data data, final IndexType type) throws IOException {
    super(data, type);
    final String prefix = fileSuffix(type);
    idxl = new DataAccess(data.meta.dbFile(prefix + 'l'));
    idxr = new DataAccess(data.meta.dbFile(prefix + 'r'));
    size = idxl.read4();
  }

  @Override
  public byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE).add(SORTED_LIST).add(NL);
    tb.add(LI_NAMES).add(data.meta.names(type)).add(NL);

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    synchronized(monitor) {
      final long l = idxl.length() + idxr.length();
      tb.add(LI_SIZE).add(Performance.formatHuman(l)).add(NL);
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
  public int size() {
    return size;
  }

  @Override
  public IndexCosts costs(final IndexSearch search) {
    if(search instanceof StringRange) return IndexCosts.get(Math.max(1, data.nodes() / 10));
    if(search instanceof NumericRange) return IndexCosts.get(Math.max(1, data.nodes() / 3));
    return IndexCosts.exact(entry(search.token()).size);
  }

  @Override
  public IndexIterator iter(final IndexSearch search) {
    final IntList pres;
    if(search instanceof final StringRange range) {
      pres = idRange(range);
    } else if(search instanceof final NumericRange range) {
      pres = idRange(range);
    } else {
      final IndexEntry ie = entry(search.token());
      pres = pres(ie.size, ie.offset);
    }

    return IndexIterator.get(pres.finish());
  }

  @Override
  public boolean drop() {
    return data.meta.drop(fileSuffix(type) + '.');
  }

  @Override
  public void close() {
    synchronized(monitor) {
      idxl.close();
      idxr.close();
    }
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();
    if(token.length == 0) return keys(0, size(), entries.descending);
    if(entries.prefix) return keys(token);

    int i = get(token);
    if(i < 0) i = -i - 1;
    return entries.descending ? keys(0, i, true) : keys(i, size(), false);
  }

  @Override
  public void flush(final boolean close) { }

  /**
   * Binary search for key in the {@code idxr} reference file.
   * <p><em>Important:</em> This method is thread-safe.</p>
   * @param key token to be found
   * @return index of the key, or (-(insertion point) - 1)
   */
  private int get(final byte[] key) {
    int l = 0, h = size - 1;
    synchronized(monitor) {
      while(l <= h) {
        final int m = l + h >>> 1;
        final byte[] txt = indexEntry(m).key;
        final int d = compare(txt, key);
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
   * @param value token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] value) {
    final IndexEntry entry = cache.get(value);
    if(entry != null) return entry;

    final long index = get(value);
    if(index < 0) return new IndexEntry(value, 0, 0);

    final int count;
    final long offset;

    synchronized(monitor) {
      // get position in heap file
      final long pos = idxr.read5(index * 5L);
      count = idxl.readNum(pos);
      offset = idxl.cursor();
    }

    return cache.add(value, count, offset);
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
   * @return sorted PRE values
   */
  private IntList pres(final int sz, final long offset) {
    final IntList pres = new IntList(sz);
    synchronized(monitor) {
      idxl.cursor(offset);
      ValueSource.refs(idxl, sz, type == IndexType.TOKEN, pres, null);
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
    final IntList pres = new IntList();
    synchronized(monitor) {
      final int i = get(tok.min());
      final int entries = size();
      for(int index = i < 0 ? -i - 1 : tok.mni() ? i : i + 1; index < entries; index++) {
        final int count = idxl.readNum(idxr.read5(index * 5L));
        int id = idxl.readNum();
        // skip traversal if value is too large
        final int diff = compare(key(id), tok.max());
        if(diff > 0 || !tok.mxi() && diff == 0) break;
        // add PRE values
        for(int c = 0; c < count; c++) {
          pres.add(id);
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
    final double min = tok.min(), max = tok.max();
    final int len = max > 0 && (long) max == max ? token(max).length : 0;
    final boolean simple = len != 0 && min > 0 && (long) min == min && token(min).length == len;

    final IntList pres = new IntList();
    synchronized(monitor) {
      final int entries = size();
      final boolean text = type == IndexType.TEXT;
      for(int index = 0; index < entries; index++) {
        final int count = idxl.readNum(idxr.read5(index * 5L));
        final int first = idxl.readNum();
        final double v = data.textDbl(first, text);
        if(v >= min && v <= max) {
          // value is in range
          for(int c = 0, pre = first; c < count; c++) {
            pres.add(pre);
            pre += idxl.readNum();
          }
        } else if(simple && v > max && data.textLen(first, text) == len) {
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
   * @param id ID of key
   * @return key token
   */
  private byte[] key(final int id) {
    final byte[] text = data.text(id, type == IndexType.TEXT);
    return type == IndexType.TOKEN ? distinctTokens(text)[idxl.readNum()] : text;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(type).add(" INDEX, '").add(data.meta.name).add("':\n");
    final int entries = size();
    for(int index = 0; index < entries; index++) {
      final long pos = idxr.read5(index * 5L);
      final int count = idxl.readNum(pos);
      int id = idxl.readNum();
      tb.add("  ").addInt(index).add(". offset: ").addLong(pos);
      tb.add(", ids: ").addInt(id);
      for(int c = 1; c < count; c++) {
        id += idxl.readNum();
        tb.add(",").addInt(id);
      }
      tb.add("\n");
    }
    return tb.toString();
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
