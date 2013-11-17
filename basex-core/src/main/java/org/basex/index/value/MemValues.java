package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class MemValues extends TokenSet implements Index {
  /** IDs. */
  int[][] ids = new int[Array.CAPACITY][];
  /** ID array lengths. */
  int[] len = new int[Array.CAPACITY];
  /** Data instance. */
  final Data data;

  /**
   * Constructor.
   * @param d data instance
   */
  public MemValues(final Data d) {
    data = d;
  }

  @Override
  public synchronized void init() { }

  @Override
  public IndexIterator iter(final IndexToken tok) {
    final byte k = tok.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    final int i = id(tok.get());
    if(i > 0) {
      final int[] pres = ids[i];
      final int s = len[i];
      if(s > 0) {
        return new IndexIterator() {
          int p;
          @Override
          public boolean more() { return p < s; }
          @Override
          public int pre() {
            while(more() && data.kind(pres[p++]) != k);
            return pres[p - 1];
          }
          @Override
          public int size() { return s; }
        };
      }
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public int costs(final IndexToken it) {
    final int i = id(it.get());
    return i == 0 ? 0 : len[i];
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] prefix = entries.get();
    return new EntryIterator() {
      int c;
      @Override
      public byte[] next() {
        while(++c < size) {
          if(startsWith(keys[c], prefix)) return keys[c];
        }
        return null;
      }
      @Override
      public int count() {
        return len[c];
      }
    };
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder(LI_STRUCTURE).add(SORTED_LIST).add(NL);
    final IndexStats stats = new IndexStats(data.meta.options.get(MainOptions.MAXSTAT));
    for(int m = 1; m < size; m++) {
      if(stats.adding(len[m])) stats.add(key(m));
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public void close() { }

  @Override
  public void rehash(final int s) {
    super.rehash(s);
    ids = Array.copyOf(ids, s);
    len = Arrays.copyOf(len, s);
  }

  /**
   * Stores the specified key and id.
   * @param key key
   * @param id id value
   * @return index id
   */
  public final int put(final byte[] key, final int id) {
    final int i = put(key);
    int[] tmp = ids[i];
    if(tmp == null) {
      tmp = new int[] { id };
    } else {
      final int l = len[i];
      if(l == tmp.length) tmp = Arrays.copyOf(tmp, Array.newSize(l));
      tmp[l] = id;
    }
    ids[i] = tmp;
    len[i]++;
    return i;
  }

  /**
   * Removes a record from the index.
   * @param key record key
   * @param id record id
   */
  @SuppressWarnings("unused")
  public void delete(final byte[] key, final int id) { }
}
