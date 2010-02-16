package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataAccess;

/**
 * This class provides access to a sorted token list.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
public final class FTSortedList {
  /** Indexed tokens. */
  private final DataAccess a;
  /** Storing pre and pos values for each token. */
  private final DataAccess b;
  /** Current FTData size. */
  private int size;
  /** Pointer on full-text data. */
  long p;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param cf current file
   * @throws IOException IO Exception
   */
  protected FTSortedList(final Data d, final int cf) throws IOException {
    this(d, Integer.toString(cf));
  }

  /**
   * Private constructor.
   * @param d data reference
   * @param s prefix
   * @throws IOException IO Exception
   */
  private FTSortedList(final Data d, final String s) throws IOException {
    a = new DataAccess(d.meta.file(DATAFTX + s + 'a'));
    b = new DataAccess(d.meta.file(DATAFTX + s + 'b'));
  }

  /**
   * Closes files.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    a.close();
    b.close();
  }

  /**
   * Returns next Token.
   * @return byte[] token
   */
  byte[] nextTok() {
    final byte tl = a.read1();
    if (tl == 0) return EMPTY;
    final long pos = a.pos();
    final byte[] tok = a.readBytes(pos, pos + tl);
    size = a.read4();
    p = a.read5();
    return tok;
  }

  /**
   * Returns next number of pre-values.
   * @return number of pre-values
   */
  int nextFTDataSize() {
    return size;
  }

  /** Next pre values. */
  int[] prv;
  /** Next pos values. */
  int[] pov;

  /**
   * Returns next pre values.
   * @return int[] pre values
   */
  int[] nextPreValues() {
    prv = new int[size];
    pov = new int[size];
    for(int j = 0; j < size; j++) {
      prv[j] = b.readNum();
      pov[j] = b.readNum();
    }
    return prv;
  }

  /**
   * Returns next pos values.
   * @return int[] pos values
   */
  int[] nextPosValues() {
    return pov;
  }
}
