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
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class provides access to attribute values and text contents
 * stored on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Values implements Index {
  /** Number of hash entries. */
  int size;
  /** ID references. */
  final DataAccess idxr;
  /** ID lists. */
  final DataAccess idxl;
  /** Value type (texts/attributes). */
  final boolean text;
  /** Values file. */
  final Data data;
  /** Cache tokens. */
  final FTTokenMap cache = new FTTokenMap();

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @throws IOException IO Exception
   */
  public Values(final Data d, final boolean txt) throws IOException {
    this(d, txt, txt ? DATATXT : DATAATV);
  }

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param txt value type (texts/attributes)
   * @param pre file prefix
   * @throws IOException IO Exception
   */
  Values(final Data d, final boolean txt, final String pre)
      throws IOException {
    data = d;
    text = txt;
    idxl = new DataAccess(d.meta.file(pre + 'l'));
    idxr = new DataAccess(d.meta.file(pre + 'r'));
    size = idxl.read4();
  }


  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(TXTINDEX + NL);
    final long l = idxl.length() + idxr.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);
    final IndexStats stats = new IndexStats(data);
    for(int m = 0; m < size; m++) {
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
    if(id > 0) return iter(cache.getSize(id), cache.getPointer(id));

    final long pos = get(tok.get());
    if(pos == 0) return IndexIterator.EMPTY;
    return iter(idxl.readNum(pos), idxl.pos());
  }

  @Override
  public int nrIDs(final IndexToken it) {
    if(it instanceof RangeToken) return idRange((RangeToken) it).size();
    final byte[] tok = it.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.getSize(id);

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
  public byte[] nextPres() {
    if(idxr.pos() >= idxr.length()) return EMPTY;
    final int s = idxl.read4();
    final long v = idxr.read5(idxr.pos());
    return idxl.readBytes(v, v + s);
  }

  /**
   * Iterator method.
   * @param s number of pre values
   * @param ps offset
   * @return iterator
   */
  private IndexIterator iter(final int s, final long ps) {
    return new IndexIterator() {
      /** Last index position. */
      long p = ps;
      /** Current position. */
      int c = -1;
      /** Last pre value. */
      int v;

      @Override
      public boolean more() {
        return ++c < s;
      }

      @Override
      public int next() {
        v += idxl.readNum(p);
        p = idxl.pos();
        return v;
      }

      @Override
      public double score() { return -1; }
    };
  }

  /**
   * Performs a range query.
   * @param tok index term
   * @return results
   */
  private IndexIterator idRange(final RangeToken tok) {
    final double min = tok.min;
    final double max = tok.max;

    final int mx = (long) max == max ? token(max).length : 0;
    final boolean sl = mx != 0 && (long) min == min && token(min).length == mx;

    final IntList ids = new IntList();
    boolean found = false;
    for(int l = 0; l < size - 1; l++) {
      final int ds = idxl.readNum(idxr.read5(l * 5L));
      int pre = idxl.readNum();
      final double v = data.textNum(pre, text);

      if(!found) {
        found = v == v;
        if(!found || v < min || v > max) continue;
      } else if(v == v) {
        // skip if if min, max and the current value have the same length
        // and if current value is larger
        if(sl && data.textLen(pre, text) == mx && v > max) break;
        if(v < min || v > max) continue;
      } else {
        // skip if all numbers have been parsed
        if(data.text(pre, text)[0] > '9') break;
        continue;
      }
      ids.add(pre);
      for(int d = 0; d < ds - 1; d++) ids.add(pre += idxl.readNum());
    }
    ids.sort();

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
   * Returns the id offset for the specified token or
   * 0 if the token is not found.
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
      final byte[] txt = data.text(pre, text);
      final int d = Token.diff(txt, key);
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
