package org.basex.index;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.util.FTTokenizer;

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
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Fulltext index.  */ FTX,
  };
  
  /**
   * Returns information on the index structure.
   * @return info
   */
  public abstract byte[] info();

  /**
   * Returns the node ids for the specified token.
   * @param tok token to be found
   * @return ids
   */
  public abstract int[] ids(final byte[] tok);

  /**
   * Returns the (approximate/estimated) number of ids for the specified token.
   * @param ft fulltext tokenizer
   * @param tok token to be found
   * @return number of ids
   */
  public abstract int nrIDs(final byte[] tok, final FTTokenizer ft);

  /**
   * Returns the decompressed ids for the specified token.
   * @param tok token to be found
   * @param ft fulltext tokenizer
   * @return ids
   */
  @SuppressWarnings("unused")
  public int[][] ftIDs(final byte[] tok, final FTTokenizer ft) {
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
   * Close the index.
   * @throws IOException in case of write errors
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }
}
