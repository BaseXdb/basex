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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class MemValues extends TokenSet implements Index {
  /** IDs. */
  int[][] ids = new int[CAP][];
  /** ID array lengths. */
  int[] len = new int[CAP];
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
          public int next() {
            while(more() && data.kind(pres[p++]) != k);
            return pres[p - 1];
          }
        };
      }
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public int count(final IndexToken it) {
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
    final IndexStats stats = new IndexStats(data.meta.prop.num(Prop.MAXSTAT));
    for(int m = 1; m < size; ++m) {
      final int oc = len[m];
      if(stats.adding(oc)) stats.add(key(m));
    }
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public void close() { }

  @Override
  public void rehash() {
    super.rehash();
    final int s = size << 1;
    ids = Array.copyOf(ids, s);
    len = Arrays.copyOf(len, s);
  }

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param id id value
   * @return index position
   */
  public int index(final byte[] key, final int id) {
    int i = add(key);
    if(i > 0) {
      ids[i] = new int[] { id };
    } else {
      i = -i;
      final int l = len[i];
      if(l == ids[i].length) ids[i] = Arrays.copyOf(ids[i], l << 1);
      ids[i][l] = id;
    }
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
