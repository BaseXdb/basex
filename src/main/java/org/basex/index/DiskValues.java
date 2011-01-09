package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This class provides access to attribute values and text contents
 * stored on disk.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class DiskValues implements Index {
  /** Number of hash entries. */
  private final int size;
  /** ID references. */
  private final DataAccess idxr;
  /** ID lists. */
  private final DataAccess idxl;
  /** Value type (texts/attributes). */
  private final boolean text;
  /** Values file. */
  private final Data data;
  /** Cache tokens. */
  private final IndexCache cache = new IndexCache();
  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final byte[][] ctext;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @throws IOException IO Exception
   */
  public DiskValues(final Data d, final boolean txt) throws IOException {
    this(d, txt, txt ? DATATXT : DATAATV);
  }

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @param pre file prefix
   * @throws IOException IO Exception
   */
  DiskValues(final Data d, final boolean txt, final String pre)
      throws IOException {
    data = d;
    text = txt;
    idxl = new DataAccess(d.meta.file(pre + 'l'));
    idxr = new DataAccess(d.meta.file(pre + 'r'));
    size = idxl.read4();
    ctext = new byte[size][];
  }

  @Override
  public byte[] info() {
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
  public IndexIterator ids(final IndexToken tok) {
    if(tok instanceof RangeToken) return idRange((RangeToken) tok);

    final int id = cache.id(tok.get());
    if(id > 0) return iter(cache.size(id), cache.pointer(id));

    final long pos = get(tok.get());
    return pos == 0 ? IndexIterator.EMPTY : iter(idxl.readNum(pos), idxl.pos());
  }

  @Override
  public int nrIDs(final IndexToken it) {
    if(it instanceof RangeToken) return idRange((RangeToken) it).size();
    final byte[] tok = it.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.size(id);

    final long pos = get(tok);
    if(pos == 0) return 0;
    final int numPre =  idxl.readNum(pos);
    cache.add(it.get(), numPre, pos + Num.len(numPre));

    return numPre;
  }

  /**
   * Returns next pre values.
   * @return compressed pre values
   */
  byte[] nextPres() {
    if(idxr.pos() >= idxr.length()) return EMPTY;
    final int s = idxl.read4();
    final long v = idxr.read5(idxr.pos());
    return idxl.readBytes(v, s);
  }

  /**
   * Iterator method.
   * @param s number of pre values
   * @param ps offset
   * @return iterator
   */
  private IndexIterator iter(final int s, final long ps) {
    final IntList ids = new IntList(s);
    long p = ps;
    for(int l = 0, v = 0; l < s; ++l) {
      v += idxl.readNum(p);
      p = idxl.pos();
      ids.add(v);
    }
    return iter(ids);
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
    final boolean simple = len != 0 && min > 0 && (long) min == min &&
      token(min).length == len;

    final IntList ids = new IntList();
    for(int l = 0; l < size; ++l) {
      final int ds = idxl.readNum(idxr.read5(l * 5L));
      int pre = idxl.readNum();
      final double v = data.textDbl(pre, text);

      if(v >= min && v <= max) {
        // value is in range
        for(int d = 0; d < ds; ++d) {
          ids.add(pre);
          pre += idxl.readNum();
        }
      } else if(simple && v > max && data.textLen(pre, text) == len) {
        // if limits are integers, if min, max and current value have the same
        // string length, and if current value is larger than max, test can be
        // skipped, as all remaining values will be bigger
        break;
      }
    }
    return iter(ids.sort());
  }

  /**
   * Returns an iterator for the specified id list.
   * @param ids id list
   * @return iterator
   */
  private IndexIterator iter(final IntList ids) {
    return new IndexIterator() {
      int p = -1;

      @Override
      public boolean more() { return ++p < ids.size(); }
      @Override
      public int next() { return ids.get(p); }
      @Override
      public double score() { return -1; }
    };
  }

  /**
   * Returns the id offset for the specified token,
   * or {@code 0} if the token is not found.
   * @param key token to be found
   * @return id offset
   */
  private long get(final byte[] key) {
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1;
      final long pos = idxr.read5(m * 5L);
      idxl.readNum(pos);
      final int pre = idxl.readNum();
      byte[] txt = ctext[m];
      if(ctext[m] == null) {
        txt = data.text(pre, text);
        ctext[m] = txt;
      }
      final int d = diff(txt, key);
      if(d == 0) return pos;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return 0;
  }

  @Override
  public void close() throws IOException {
    idxl.close();
    idxr.close();
  }
}
