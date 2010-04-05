package org.basex.query.expr;

/**
 * Return types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Return {
  /** Single Boolean.        */ BLN(true, false),
  /** Non-numeric item.      */ NONUM(true, false),
  /** Non-numeric sequence.  */ NONUMSEQ(false, false),
  /** Node.                  */ NOD(true, false),
  /** Nodes.                 */ NODSEQ(false, false),
  /** Single Number.         */ NUM(true, true),
  /** Numeric sequence.      */ NUMSEQ(false, true),
  /** Sequence.              */ SEQ(false, true),
  /** String.                */ STR(false, false);

  /** Single result (always returns one value). */
  public boolean single;
  /** Numeric result (return type could be numeric). */
  public boolean num;

  /**
   * Constructor.
   * @param s single result
   * @param n numeric flag
   */
  private Return(final boolean s, final boolean n) {
    single = s;
    num = n;
  }
}
