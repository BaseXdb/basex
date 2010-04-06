package org.basex.query.expr;

/**
 * Return types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Return {
  /** Single Boolean.       */ BLN(true, false),
  /** Boolean sequence.     */ BLNSEQ(false, false),
  /** Non-numeric item.     */ NONUM(true, false),
  /** Non-numeric sequence. */ NONUMSEQ(false, false),
  /** Node.                 */ NOD(true, false),
  /** Nodes.                */ NODSEQ(false, false),
  /** Single Number.        */ NUM(true, true),
  /** Numeric sequence.     */ NUMSEQ(false, true),
  /** Sequence.             */ SEQ(false, true),
  /** String item.          */ STR(true, false),
  /** String sequence.      */ STRSEQ(false, false);

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
  
  /**
   * Returns the sequence variant of the specified return type. 
   * @return sequence variant
   */
  public Return seq() {
    if(this == BLN) return BLNSEQ;
    if(this == NONUM) return NONUMSEQ;
    if(this == NOD) return NODSEQ;
    if(this == NUM) return NUMSEQ;
    if(this == STR) return STRSEQ;
    return this;
  }
  
  /**
   * Returns the single variant of the specified return type. 
   * @return single variant
   */
  public Return single() {
    if(this == BLNSEQ) return BLN;
    if(this == NONUMSEQ) return NONUM;
    if(this == NODSEQ) return NOD;
    if(this == NUMSEQ) return NUM;
    if(this == STRSEQ) return STR;
    return this;
  }
}
