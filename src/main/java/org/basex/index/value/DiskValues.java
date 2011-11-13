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
import org.basex.util.list.IntList;

/**
 * This class provides access to attribute values and text contents stored on
 * disk. The data structure is described in the {@link ValueBuilder} class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DiskValues implements Index {
  /** Number of index entries. */
  private final int size;
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
      final int oc = idxl.readNum(idxr.read5(m * 5L));
      if(stats.adding(oc)) stats.add(data.text(idxl.readNum(), text));
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken tok) {
    if(tok instanceof RangeToken) return idRange((RangeToken) tok);

    final int id = cache.id(tok.get());
    if(id > 0) return iter(cache.size(id), cache.pointer(id));

    final long pos = get(tok.get());
    return pos == 0 ? IndexIterator.EMPTY : iter(idxl.readNum(pos),
        idxl.cursor());
  }

  @Override
  public synchronized int count(final IndexToken it) {
    if(it instanceof RangeToken) return idRange((RangeToken) it).size();
    if(it.get().length > MAXLEN) return Integer.MAX_VALUE;

    final byte[] tok = it.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.size(id);

    final long pos = get(tok);
    if(pos == 0) return 0;
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
      pres.add(v);
    }
    return iter(pres);
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
   * Returns the id offset for the specified token, or {@code 0} if the token is
   * not found.
   * @param key token to be found
   * @return id offset
   */
  private synchronized long get(final byte[] key) {
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final long pos = idxr.read5(m * 5L);
      // get text from cache, of add entry to cache
      byte[] txt = ctext.get(m);
      if(txt == null) {
        idxl.readNum(pos);
        txt = data.text(idxl.readNum(), text);
        ctext.add(m, txt);
      }
      final int d = diff(txt, key);
      if(d == 0) return pos;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return 0;
  }

  @Override
  public synchronized void close() throws IOException {
    idxl.close();
    idxr.close();
  }
}
