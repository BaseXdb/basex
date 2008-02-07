package org.basex.index;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.io.PrintOutput;
import org.basex.query.xpath.expr.FTOption;
import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This class provides a main-memory access to attribute values and
 * text contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemValues extends Set implements Index {
  /** IDs. */
  private int[][] ids = new int[CAP][];
  /** ID array lengths. */
  private int[] len = new int[CAP];
  
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
      if(ids[i].length == l) ids[i] = Array.extend(ids[i]);
      ids[i][l] = id;
    }
    len[i]++;
    return i;
  }

  /**
   * Returns all ids from index stored for tok with respect to ftO.
   * @param tok token to be found
   * @param ftO FTOption for tok
   * @return number of ids
   */
  public int[] ids(final byte[] tok, final FTOption ftO) {
    if(ftO != null) BaseX.debug("Values: No fulltext option support.");
    return null;
  }

  /**
   * Returns all ids from index stored for tok with respect to ftO.
   * @param tok token to be found
   * @param ftO FTOption for tok
   * @return number of ids
   */
  public int[][] idPos(final byte[] tok, final FTOption ftO) {
    if(ftO != null) BaseX.debug("Values: No fulltext option support.");
    return null;
  }
  
  /**
   * Returns all ids that are in the range of tok0 and tok1.
   * @param tok0 token defining range start
   * @param itok0 token included in rangebounderies
   * @param tok1 token defining range end
   * @param itok1 token included in rangebounderies
   * @return number of ids
   */
  public int[][]  idPosRange(final byte[] tok0, final boolean itok0, 
      final byte[] tok1, final boolean itok1) {
   BaseX.debug("Words: No fulltext range query support.");
   return null;
  }


  
  /**
   * Returns the id for the specified key.
   * @param key key
   * @return id (negative if value wasn't found)
   */
  public int get(final byte[] key) {
    return -add(key);
  }

  /**
   * Returns the token for the specified id.
   * @param id id
   * @return token
   */
  public byte[] token(final int id) {
    return keys[id];
  }
  
  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or null if nothing was found
   */
  public int[] ids(final byte[] key) {
    final int i = id(key);
    return i == 0 ? Array.NOINTS : Array.finish(ids[i], len[i]);
  }
  
  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or null if nothing was found
   */
  public int nrIDs(final byte[] key) {
    return len[id(key)];
  }

  @Override
  protected void rehash() {
    super.rehash();
    ids = Array.extend(ids);
    len = Array.extend(len);
  }

  /**
   * Returns information on the index structure.
   * @param out output stream
   * @throws IOException in case of write errors
   */
  public void info(final PrintOutput out) throws IOException {
    out.print("MemValues");
  }

  /**
   * Close the index.
   */
  public void close() { }
}
