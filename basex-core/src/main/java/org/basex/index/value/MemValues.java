package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides main memory access to attribute values and text contents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MemValues extends ValueIndex {
  /** Values. */
  private final TokenSet values;
  /** IDs lists. */
  private ArrayList<int[]> ids;
  /** ID array lengths. */
  private IntList len;

  /**
   * Constructor.
   * @param data data instance
   * @param text value type (texts/attributes)
   */
  public MemValues(final Data data, final boolean text) {
    super(data, text);
    values = ((MemData) data).values(text);
    final int s = size() + 1;
    ids = new ArrayList<>(s);
    len = new IntList(s);
  }

  @Override
  public IndexIterator iter(final IndexToken token) {
    final int id = values.id(token.get());
    if(id > 0) {
      final int s = len.get(id);
      final int[] tmp = ids.get(id);
      final IntList pres = new IntList(s);
      for(int i = 0; i < s; ++i) pres.add(data.pre(tmp[i]));
      pres.sort();

      return new IndexIterator() {
        int p = -1;
        @Override
        public boolean more() { return ++p < s; }
        @Override
        public int pre() { return pres.get(p); }
        @Override
        public int size() { return s; }
      };
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public int costs(final IndexToken it) {
    return len.get(values.id(it.get()));
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] prefix = entries.get();
    return new EntryIterator() {
      final int s = size();
      int c;
      @Override
      public byte[] next() {
        while(++c < s) {
          final byte[] key = values.key(c);
          if(startsWith(key, prefix) && ids.get(c).length > 0) return key;
        }
        return null;
      }
      @Override
      public int count() {
        return len.get(c);
      }
    };
  }

  @Override
  public byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE).add(HASH).add(NL);
    tb.add(LI_NAMES).add(text ? data.meta.textinclude : data.meta.attrinclude).add(NL);

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    final int s = size();
    for(int c = 1; c < s; c++) {
      final int oc = len.get(c);
      if(stats.adding(oc)) stats.add(values.key(c), oc);
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public boolean drop() {
    ids = null;
    len = null;
    return true;
  }

  @Override
  public void add(final TokenObjMap<IntList> map) {
    for(final byte[] key : new TokenList(map)) add(key, map.get(key).sort().finish());
  }

  @Override
  public void delete(final TokenObjMap<IntList> map) {
    for(final byte[] key : new TokenList(map)) delete(key, map.get(key).sort().finish());
  }

  @Override
  public void flush() { }

  @Override
  public void close() { }

  /**
   * Adds values to the index.
   * @param key key to be indexed
   * @param vals sorted values
   */
  void add(final byte[] key, final int... vals) {
    final int id = values.id(key), vl = vals.length;
    final int l = id < len.size() ? len.get(id) : 0, s = l + vl;
    int[] tmp = id < len.size() ? ids.get(id) : null;
    if(tmp == null) {
      tmp = vals;
    } else {
      if(l >= tmp.length) tmp = Arrays.copyOf(tmp, Array.newSize(l));
      System.arraycopy(vals, 0, tmp, l, vl);
    }
    while(id + 1 > ids.size()) ids.add(null);
    ids.set(id, tmp);
    len.set(id, s);
  }

  /**
   * Removes values from the index.
   * @param key key
   * @param vals sorted values
   */
  void delete(final byte[] key, final int... vals) {
    final int id = values.id(key), vl = vals.length, l = len.get(id);
    final int[] tmp = ids.get(id);
    int o = -1, n = 0, v = 0;
    while(++o < l) {
      if(v == vl || tmp[o] != vals[v++]) tmp[n++] = tmp[o];
    }
    len.set(id, len.get(id) - vl);
  }
}
