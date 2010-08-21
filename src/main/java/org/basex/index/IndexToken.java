package org.basex.index;

/**
 * This class defines access to index tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public interface IndexToken {
  /** Index types. */
  public enum IndexType {
    /** Attribute index. */ ATTN,
    /** Tag index.       */ TAG,
    /** Text index.      */ TEXT,
    /** Attribute index. */ ATTV,
    /** Full-text index. */ FTXT,
    /** Path index. */      PATH
  }

  /**
   * Returns the index type.
   * @return type
   */
  IndexType type();

  /**
   * Returns the current token.
   * @return token
   */
  byte[] get();
}
