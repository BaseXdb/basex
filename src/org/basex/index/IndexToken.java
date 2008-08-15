package org.basex.index;

import org.basex.util.Token;

/**
 * This class defines access to index tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class IndexToken {
  /** Index types. */
  public enum TYPE {
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Fulltext index.  */ FTX,
  };
  /** Text. */
  public byte[] text = Token.EMPTY;
  /** Index type. */
  public TYPE type;

  /**
   * Constructor.
   * @param t index type
   */
  public IndexToken(final TYPE t) {
    type = t;
  }
  
  /**
   * Returns the current index token. Can be overwritten by an implementation
   * to return other tokens, as is done in the {@link FTTokenizer}.
   * @return token
   */
  public byte[] get() {
    return text;
  }
  
  /**
   * Returns the range flag. Can be overwritten by an implementation to
   * support range searches.
   * @return token
   */
  public boolean range() {
    return false;
  }
}
