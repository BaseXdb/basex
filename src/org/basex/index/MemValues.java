package org.basex.index;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
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

  /** {@inheritDoc} */
  public int[][] idPos(final byte[] tok, final FTOption ftO, final Data d) {
    BaseX.notimplemented();
    return null;
  }
  
  /** {@inheritDoc} */
  public int[][]  idPosRange(final byte[] tok0, final boolean itok0, 
      final byte[] tok1, final boolean itok1) {
    BaseX.notimplemented();
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
  
  /** {@inheritDoc} */
  public int[] ids(final byte[] key) {
    final int i = id(key);
    return i == 0 ? Array.NOINTS : Array.finish(ids[i], len[i]);
  }
  
  /** {@inheritDoc} */
  public int nrIDs(final byte[] key) {
    return len[id(key)];
  }
  
  /** {@inheritDoc} */
  public int[][] fuzzyIDs(final byte[] tok, final int ne) {
    BaseX.notimplemented();
    return null;
  }
  

  @Override
  protected void rehash() {
    super.rehash();
    ids = Array.extend(ids);
    len = Array.extend(len);
  }

  /** {@inheritDoc} */
  public void info(final PrintOutput out) throws IOException {
    out.print("MemValues");
  }

  /** {@inheritDoc} */
  public void close() { }
}
