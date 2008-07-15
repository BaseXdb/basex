package org.basex.index;

import org.basex.util.Array;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class provides a main-memory access to attribute values and
 * text contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemValues extends Index {
  /** Initial hash capacity. */
  private final MemValueIndex index = new MemValueIndex();

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param id id value
   * @return index position
   */
  public int index(final byte[] key, final int id) {
    return index.index(key, id);
  }

  /**
   * Returns the id for the specified key.
   * @param key key
   * @return id (negative if value wasn't found)
   */
  public int get(final byte[] key) {
    return -index.id(key);
  }

  /**
   * Returns the token for the specified id.
   * @param id id
   * @return token
   */
  public byte[] token(final int id) {
    return index.key(id);
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    return index.ids(ind.text);
  }

  @Override
  public int nrIDs(final IndexToken ind) {
    return ids(ind).size();
  }

  @Override
  public byte[] info() {
    return Token.token("MemValues");
  }

  /** MemValue Index. */
  static final class MemValueIndex extends Set {
    /** IDs. */
    int[][] ids = new int[CAP][];
    /** ID array lengths. */
    int[] len = new int[CAP];

    /**
     * Indexes the specified keys and values.
     * @param key key
     * @param id id value
     * @return index position
     */
    int index(final byte[] key, final int id) {
      int i = add(key);
      if(i > 0) {
        ids[i] = new int[] { id };
      } else {
        i = -i;
        final int l = len[i];
        if(ids[i].length == l) ids[i] = Array.extend(ids[i]);
        ids[i][l] = id;
      }
      len[i]++;
      return i;
    }

    /**
     * Returns the ids for the specified key.
     * @param key index key
     * @return ids
     */
    IndexIterator ids(final byte[] key) {
      final int i = id(key);
      return i == 0 ? IndexIterator.EMPTY :
        new IndexArrayIterator(ids[i], len[i]);
    }

    @Override
    public void rehash() {
      super.rehash();
      ids = Array.extend(ids);
      len = Array.extend(len);
    }
  }
}
