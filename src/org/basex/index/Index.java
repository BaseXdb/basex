package org.basex.index;

import java.io.IOException;
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
public interface Index {
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
  void info(PrintOutput out) throws IOException;

  /**
   * Returns the node ids for the specified token.
   * @param tok token to be found
   * @return ids
   */
  int[] ids(final byte[] tok);

  /**
   * Returns the decompressed ids for the specified token.
   * @param tok token to be found
   * @param ftO ftoption for token to be found
   * @param d data reference
   * @return ids
   */
  int[][] idPos(final byte[] tok, final FTOption ftO, final Data d);

  /**
   * FUZZY SEARCH
   * Returns the indexed id references for the specified fulltext token,
   * with respect to number of errors (ne) that are allowed to occure.
   * 
   * @param fulltext token to be looked up
   * @param ne int number of errors allowed
   * @return id array
   */
  int[][] fuzzyIDs(byte[] fulltext, final int ne);

  
  /**
   * Returns the decompressed ids for the specified range expression.
   * Each token between tok0 and tok1 is returned as result.
   * @param tok0 start token defining the range
   * @param itok0 token included in rangebounderies
   * @param tok1 end token defining the range
   * @param itok1 token included in rangebounderies
   * @return ids
   */
  int[][] idPosRange(final byte[] tok0, final boolean itok0, 
      final byte[] tok1, final boolean itok1);


  /**
   * Returns the (approximate/estimated) number of ids for the specified token.
   * @param tok token to be found
   * @return number of ids
   */
  int nrIDs(final byte[] tok);

  /**
   * Close the index.
   * @throws IOException in case of write errors
   */
  void close() throws IOException;
}
