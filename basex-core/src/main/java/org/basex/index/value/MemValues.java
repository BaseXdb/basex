package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides main memory access to attribute values and text contents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MemValues extends ValueIndex {
  /** Values. */
  private final TokenSet values;
  /** IDs lists. */
  private ArrayList<int[]> idsList;
  /** ID array lengths. */
  private IntList lenList;
  /** Order flags. */
  private BoolList reorder;

  /**
   * Constructor.
   * @param data data instance
   * @param type index type
   */
  public MemValues(final Data data, final IndexType type) {
    super(data, type);
    // token index: work extra token set instance
    values = type == IndexType.TOKEN ? new TokenSet() :
      ((MemData) data).values(type == IndexType.TEXT);
    final int s = values.size() + 1;
    idsList = new ArrayList<>(s);
    lenList = new IntList(s);
    reorder = new BoolList(s);
  }

  @Override
  public IndexIterator iter(final IndexSearch search) {
    final int id = values.id(search.token());
    if(id == 0) return IndexIterator.EMPTY;

    final int len = lenList.get(id);
    final int[] ids = idsList.get(id), pres;
    if(data.meta.updindex) {
      final IntList tmp = new IntList();
      for(int i = 0; i < len; ++i) tmp.add(data.pre(ids[i]));
      pres = tmp.sort().finish();
    } else {
      pres = ids;
    }

    return new IndexIterator() {
      int p;
      @Override
      public boolean more() { return p < len; }
      @Override
      public int pre() { return pres[p++]; }
      @Override
      public int size() { return len; }
    };
  }

  @Override
  public IndexCosts costs(final IndexSearch search) {
    return IndexCosts.get(lenList.get(values.id(search.token())));
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();

    return new EntryIterator() {
      final int s = values.size();
      int p;

      @Override
      public byte[] next() {
        while(++p <= s) {
          if(lenList.get(p) == 0) continue;
          final byte[] key = values.key(p);
          if(startsWith(key, token)) return key;
        }
        return null;
      }

      @Override
      public int count() {
        return lenList.get(p);
      }
    };
  }

  @Override
  public byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE).add(HASH).add(NL);
    tb.add(LI_NAMES).add(data.meta.names(type)).add(NL);

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    final int s = values.size();
    for(int p = 1; p <= s; p++) {
      final int oc = lenList.get(p);
      if(oc > 0 && stats.adding(oc)) stats.add(values.key(p), oc);
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int size() {
    // returns the actual number of indexed entries
    final int ll = lenList.size();
    int s = 0;
    for(int c = 1; c < ll; c++) {
      if(lenList.get(c) > 0) s++;
    }
    return s;
  }

  @Override
  public boolean drop() {
    idsList = null;
    lenList = null;
    return true;
  }

  @Override
  public void add(final ValueCache cache) {
    for(final byte[] key : cache) {
      final IntList ids = cache.ids(key);
      if(!ids.isEmpty()) add(key, ids.sort().finish());
    }
    finish();
  }

  @Override
  public void delete(final ValueCache cache) {
    for(final byte[] key : cache) {
      delete(key, cache.ids(key).sort().finish());
    }
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
    // token index: add values. otherwise, reference existing values
    final int id = type == IndexType.TOKEN ? values.put(key) : values.id(key), vl = vals.length;
    // updatable index: if required, resize existing arrays
    while(idsList.size() < id + 1) idsList.add(null);
    if(lenList.size() < id + 1) lenList.set(id, 0);

    final int len = lenList.get(id), size = len + vl;
    int[] ids = idsList.get(id);
    if(ids == null) {
      ids = vals;
    } else {
      if(ids.length < size) ids = Arrays.copyOf(ids, Array.newCapacity(size));
      Array.copyFromStart(vals, vl, ids, len);
      if(ids[len - 1] > vals[0]) {
        if(reorder == null) reorder = new BoolList(values.size());
        reorder.set(id, true);
      }
    }
    idsList.set(id, ids);
    lenList.set(id, size);
  }

  /**
   * Finishes the index creation.
   */
  void finish() {
    if(reorder == null) return;
    for(int i = 1; i < reorder.size(); i++) {
      if(reorder.get(i)) Arrays.sort(idsList.get(i), 0, lenList.get(i));
    }
    reorder = null;
  }

  /**
   * Removes values from the index.
   * @param key key
   * @param vals sorted values
   */
  void delete(final byte[] key, final int... vals) {
    final int id = values.id(key), vl = vals.length, l = lenList.get(id), s = l - vl;
    final int[] ids = idsList.get(id);
    for(int i = 0, n = 0, v = 0; i < l; i++) {
      if(v == vl || ids[i] != vals[v]) ids[n++] = ids[i];
      else v++;
    }
    lenList.set(id, s);
    if(s == 0) idsList.set(id, null);
  }

  /**
   * Returns a string representation of the index structure.
   * @param all include database contents in the representation. During updates, database lookups
   *        must be avoided, as the data structures will be inconsistent
   * @return string
   */
  public String toString(final boolean all) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(type).add(" INDEX, '").add(data.meta.name).add("':\n");
    final int s = lenList.size();
    for(int m = 1; m < s; m++) {
      final int len = lenList.get(m);
      if(len == 0) continue;
      final int[] ids = idsList.get(m);
      tb.add("  ").addInt(m);
      if(all) tb.add(", key: \"").add(data.text(data.pre(ids[0]), type == IndexType.TEXT)).add('"');
      tb.add(", ids");
      if(all) tb.add("/pres");
      tb.add(": ");
      for(int n = 0; n < len; n++) {
        if(n != 0) tb.add(",");
        tb.addInt(ids[n]);
        if(all) tb.add('/').addInt(data.pre(ids[n]));
      }
      tb.add("\n");
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return toString(false);
  }
}
