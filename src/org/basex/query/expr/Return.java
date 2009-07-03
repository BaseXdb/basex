package org.basex.query.expr;

/**
 * Return types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public enum Return {
  /** Single Boolean.        */ BLN(true, false, false),
  /** Non-numeric item.      */ NONUM(true, false, true),
  /** Non-numeric sequence.  */ NONUMSEQ(false, false, true),
  /** Node.                  */ NOD(true, false, true),
  /** Nodes.                 */ NODSEQ(false, false, true),
  /** Single Number.         */ NUM(true, true, false),
  /** Numeric sequence.      */ NUMSEQ(false, true, false),
  /** Sequence.              */ SEQ(false, true, true),
  /** String.                */ STR(false, false, false);

  /** Single result (always returns one value). */
  public boolean single;
  /** Numeric result (return type could be numeric). */
  public boolean num;
  /** Node result (return type could be a node). */
  public boolean node;

  /**
   * Constructor.
   * @param s single result
   * @param n numeric flag
   * @param d node flag
   */
  Return(final boolean s, final boolean n, final boolean d) {
    single = s;
    num = n;
    node = d;
  }
}
