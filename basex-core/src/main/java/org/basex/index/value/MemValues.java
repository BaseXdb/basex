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

  /** Order flags. */
  private BoolList orders;

  /**
   * Constructor.
   * @param data data instance
   * @param text value type (texts/attributes)
   */
  public MemValues(final Data data, final boolean text) {
    super(data, text);
    values = ((MemData) data).values(text);
    final int s = values.size() + 1;
    ids = new ArrayList<>(s);
    len = new IntList(s);
    orders = new BoolList(s);
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
      final int s = values.size();
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
    final int s = values.size();
    for(int c = 1; c < s; c++) {
      final int oc = len.get(c);
      if(stats.adding(oc)) stats.add(values.key(c), oc);
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int size() {
    // returns the actual number of indexed entries
    int s = 0;
    for(int c = 1; c < s; c++) {
      if(len.get(c) > 0) s++;
    }
    return s;
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
    finish();
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
    if(vals.length == 0) return;

    // if required, resize existing arrays
    final int id = values.id(key), vl = vals.length;
    while(ids.size() < id + 1) ids.add(null);
    if(len.size() < id + 1) len.set(id, 0);

    final int l = len.get(id), s = l + vl;
    int[] tmp = ids.get(id);
    if(tmp == null) {
      tmp = vals;
    } else {
      if(tmp.length < s) tmp = Arrays.copyOf(tmp, Array.newSize(s));
      System.arraycopy(vals, 0, tmp, l, vl);
      if(tmp[l - 1] > vals[0]) {
        if(orders == null) orders = new BoolList(values.size());
        orders.set(id, true);
      }
    }
    ids.set(id, tmp);
    len.set(id, s);
  }

  /**
   * Finishes the index creation.
   */
  void finish() {
    if(orders == null) return;
    for(int i = 1; i < orders.size(); i++) {
      if(orders.get(i)) Arrays.sort(ids.get(i), 0, len.get(i));
    }
    orders = null;
  }

  /**
   * Removes values from the index.
   * @param key key
   * @param vals sorted values
   */
  void delete(final byte[] key, final int... vals) {
    final int id = values.id(key), vl = vals.length, l = len.get(id), s = l - vl;
    final int[] tmp = ids.get(id);
    int o = -1, n = 0, v = 0;
    while(++o < l) {
      if(v == vl || tmp[o] != vals[v++]) tmp[n++] = tmp[o];
    }
    len.set(id, s);
    if(s == 0) ids.set(id, null);
  }

  /**
   * Returns a string representation of the index structure.
   * @param all include database contents in the representation
   * @return string
   */
  public String toString(final boolean all) {
    final int sz = values.size();
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text ? "TEXT" : "ATTRIBUTE").add(" INDEX, '").add(data.meta.name).add("':\n");
    if(sz != 0) {
      for(int m = 1; m <= sz; m++) {
        final int oc = len.get(m);
        if(oc == 0) continue;
        final int[] pos = ids.get(m);
        tb.add("  ").addInt(m);
        if(all) tb.add(", key: \"").add(data.text(data.pre(pos[0]), text)).add('"');
        tb.add(", ids");
        if(all) tb.add("/pres");
        tb.add(": ");
        for(int n = 0; n < oc; n++) {
          if(n != 0) tb.add(",");
          tb.addInt(pos[n]);
          if(all) tb.add('/').addInt(data.pre(pos[n]));
        }
        tb.add("\n");
      }
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return toString(false);
  }
}
