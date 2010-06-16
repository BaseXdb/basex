package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.File;
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
  /** Empty integer array. */
  private static final int[] NOINTS = {};

  /** Storing pre and pos values for each token. */
  private final DataAccess dat;
  /** Structure file. */
  private final File files;
  /** Data file. */
  private final File filed;
  /** Wasted flag. */
  private boolean wasted;

  /** Indexed tokens. */
  final DataAccess str;
  /** Current data size. */
  int size;
  /** Next token. */
  byte[] tok;
  /** Next pre values. */
  int[] prv;
  /** Next pos values. */
  int[] pov;

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
    files = d.meta.file(DATAFTX + p + ss);
    filed = d.meta.file(DATAFTX + p + ds);
    str = new DataAccess(files);
    dat = new DataAccess(filed);
  }

  /**
   * Checks if more tokens are found.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    if(wasted) return;

    tok = token();
    if(tok.length == 0) {
      wasted = true;
      prv = NOINTS;
      pov = NOINTS;
      close();
    } else {
      prv = new int[size];
      pov = new int[size];
      for(int j = 0; j < size; j++) {
        prv[j] = dat.readNum();
        pov[j] = dat.readNum();
      }
    }
  }

  /**
   * Closes and deletes the input files.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    str.close();
    dat.close();
    files.delete();
    filed.delete();
  }

  /**
   * Returns next token.
   * @return byte[] token
   */
  protected abstract byte[] token();
}
