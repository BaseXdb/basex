package org.basex.index;

import org.basex.ft.Tokenizer;
import org.basex.util.Token;

/**
 * This class defines access to index tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class IndexToken {
  /** Index types. */
  public enum Type {
    /** Attribute index. */ ATN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TXT,
    /** Attribute index. */ ATV,
    /** Fulltext index.  */ FTX,
  };
  /** Text. */
  public byte[] text = Token.EMPTY;
  /** Index type. */
  public Type type;

  /**
   * Constructor.
   * @param t index type
   */
  public IndexToken(final Type t) {
    type = t;
  }
  
  /**
   * Returns the current index token. Can be overwritten by an implementation
   * to return other tokens, as is done in the {@link Tokenizer}.
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
