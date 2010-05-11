package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataAccess;

/**
 * This class provides temporary access to sorted list data.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
abstract class FTList {
  /** Indexed tokens. */
  protected final DataAccess str;
  /** Storing pre and pos values for each token. */
  protected final DataAccess dat;
  /** Current data size. */
  protected int size;
  /** Next pre values. */
  private int[] prv;
  /** Next pos values. */
  private int[] pov;

  /**
   * Constructor, initializing the index structure.
   * @param d data
   * @param p prefix
   * @param ss structure
   * @param ds structure
   * @throws IOException I/O exception
   */
  protected FTList(final Data d, final int p, final char ss, final char ds)
      throws IOException {
    str = new DataAccess(d.meta.file(DATAFTX + p + ss));
    dat = new DataAccess(d.meta.file(DATAFTX + p + ds));
  }

  /**
   * Closes files.
   * @throws IOException I/O exception
   */
  final void close() throws IOException {
    str.close();
    dat.close();
  }

  /**
   * Returns next token.
   * @return byte[] token
   */
  abstract byte[] next();

  /**
   * Returns next number of pre-values.
   * @return number of pre-values
   */
  final int size() {
    return size;
  }

  /**
   * Returns next pre values.
   * @return int[] pre values
   */
  final int[] pres() {
    prv = new int[size];
    pov = new int[size];
    for(int j = 0; j < size; j++) {
      prv[j] = dat.readNum();
      pov[j] = dat.readNum();
    }
    return prv;
  }

  /**
   * Returns next pos values.
   * @return int[] pos values
   */
  final int[] poss() {
    return pov;
  }
}
