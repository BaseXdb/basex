package org.basex.index;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.query.xpath.expr.FTOption;
import org.basex.data.Data;
import org.basex.io.PrintOutput;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Index {
  /** Index types. */
  public enum TYPE {
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Fulltext index.  */ FTX,
    /** Fuzzy index.     */ FUY;
  };
  
  /**
   * Returns information on the index structure.
   * @param out output stream
   * @throws IOException in case of write errors
   */
  public abstract void info(PrintOutput out) throws IOException;

  /**
   * Returns the node ids for the specified token.
   * @param tok token to be found
   * @return ids
   */
  public abstract int[] ids(final byte[] tok);

  /**
   * Returns the (approximate/estimated) number of ids for the specified token.
   * @param tok token to be found
   * @return number of ids
   */
  public abstract int nrIDs(final byte[] tok);

  /**
   * Returns the decompressed ids for the specified token.
   * @param tok token to be found
   * @param ftO ftoption for token to be found
   * @param d data reference
   * @return ids
   */
  @SuppressWarnings("unused")
  public int[][] ftIDs(final byte[] tok, final FTOption ftO,
      final Data d) {
    BaseX.notimplemented();
    return null;
  }

  /**
   * Returns the decompressed ids for the specified range expression.
   * Each token between tok0 and tok1 is returned as result.
   * @param tok0 start token defining the range
   * @param itok0 token included in range boundaries
   * @param tok1 end token defining the range
   * @param itok1 token included in range boundaries
   * @return ids
   */
  @SuppressWarnings("unused")
  public int[] idRange(final double tok0, final boolean itok0, 
      final double tok1, final boolean itok1) {
    BaseX.notimplemented();
    return null;
  }

  /**
   * FUZZY SEARCH
   * Returns the indexed id references for the specified fulltext token,
   * with respect to number of errors (ne) that are allowed to occur.
   * 
   * @param tok token to look up
   * @param ne int number of errors allowed
   * @return id array
   */
  @SuppressWarnings("unused")
  public int[][] fuzzyIDs(final byte[] tok, final int ne) {
    BaseX.notimplemented();
    return null;
  }

  /**
   * Close the index.
   * @throws IOException in case of write errors
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }
}
