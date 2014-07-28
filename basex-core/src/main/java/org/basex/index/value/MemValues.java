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
 * This class provides main memory access to attribute values and text contents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MemValues extends TokenSet implements Index {
  /** Updating index. */
  private final boolean updindex;
  /** Indexing flag. */
  private boolean index = true;
  /** Data instance. */
  private final Data data;
  /** IDs. */
  private int[][] ids = new int[Array.CAPACITY][];
  /** ID array lengths. */
  private int[] len = new int[Array.CAPACITY];

  /**
   * Constructor.
   * @param data data instance
   * @param updindex updating index
   */
  public MemValues(final Data data, final boolean updindex) {
    this.data = data;
    this.updindex = updindex;
  }

  @Override
  public void init() { }

  @Override
  public IndexIterator iter(final IndexToken token) {
    final byte k = token.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    final int i = id(token.get());
    if(i > 0) {
      final int[] pres = updindex ? data.pre(ids[i], 0, len[i]) : ids[i];
      final int s = updindex ? pres.length : len[i];
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
  public int costs(final IndexToken token) {
    final int i = id(token.get());
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
    final TokenBuilder tb = new TokenBuilder(LI_STRUCTURE).add(HASH).add(NL);
    final IndexStats stats = new IndexStats(data.meta.options.get(MainOptions.MAXSTAT));
    for(int m = 1; m < size; m++) {
      if(stats.adding(len[m])) stats.add(key(m));
    }
    stats.print(tb);
    return tb.finish();
  }

  /**
   * Finishes the build process.
   */
  public void finish() {
    index = updindex;
  }

  /**
   * Creates an index.
   * @param type index type
   */
  public void create(final IndexType type) {
    final byte kind = type == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    ids = new int[keys.length][];
    len = new int[keys.length];
    index = true;
    final int s = data.meta.size;
    for(int p = 0; p < s; p++) {
      if(data.kind(p) == kind) put(key((int) data.textOff(p)), data.id(p));
    }
    finish();
  }

  @Override
  public boolean drop() {
    ids = null;
    len = null;
    index = false;
    return true;
  }

  @Override
  public void close() { }

  @Override
  public void rehash(final int s) {
    super.rehash(s);
    if(ids != null) {
      ids = Array.copyOf(ids, s);
      len = Arrays.copyOf(len, s);
    }
  }

  /**
   * Stores the specified key and id.
   * @param key key
   * @param id id value
   * @return index id
   */
  public int put(final byte[] key, final int id) {
    // new entries must be indexed, but inverted index structures will be invalidated
    final int i = put(key);
    if(index()) {
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
    }
    return i;
  }

  /**
   * Removes a record from the index.
   * @param key record key
   * @param id record id
   */
  public void delete(final byte[] key, final int id) {
    if(index()) {
      final int i = id(key);
      if(i == 0 || len[i] == 0) return;

      // find the position where the id is stored
      int p = -1;
      while(++p < len[i]) if(ids[i][p] == id) break;

      // if not the last element, we need to shift forwards
      if(p < len[i] - 1) Array.move(ids[i], p + 1, -1, len[i] - (p + 1));
      len[i]--;
    }
  }

  /**
   * Checks if full index structure is to be updated.
   * @return result of check
   */
  private boolean index() {
    if(index) return true;
    if(ids != null) drop();
    return false;
  }
}
