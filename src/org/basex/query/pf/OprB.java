package org.basex.query.pf;

import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.query.pf.PFT.*;

/**
 * This class generates new XQuery values.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class OprB {
  /** Operator names. */
  private static final Op[] OP = {
    new Op(ALL, All.class), new Op(AND, And.class), new Op(ATR, Atr.class),
    new Op(AVG, Avg.class), new Op(ACH, Ach.class), new Op(CST, Cst.class),
    new Op(CNT, Cnt.class), new Op(CRS, Crs.class), new Op(DIF, Dif.class),
    new Op(DOC, Doc.class), new Op(DOA, DoA.class), new Op(DST, Dst.class),
    new Op(ELM, Elm.class), new Op(EQU, Equ.class), new Op(EQJ, EqJ.class),
    new Op(ERR, Err.class), new Op(FRE, FrE.class), new Op(FRU, FrU.class),
    new Op(FRG, Frg.class), new Op(FUN, Fun.class), new Op(GRT, Grt.class),
    new Op(ISC, ISc.class), new Op(MAX, Max.class), new Op(MRG, Mrg.class),
    new Op(MIN, Min.class), new Op(NOT, Not.class), new Op(NUM, Num.class),
    new Op(ORR, Orr.class), new Op(PRJ, Prj.class), new Op(ROO, Roo.class),
    new Op(RON, RoN.class), new Op(SCJ, ScJ.class), new Op(SEL, Sel.class),
    new Op(SMJ, SmJ.class), new Op(SEQ, Seq.class), new Op(SER, Ser.class),
    new Op(STJ, StJ.class), new Op(STV, StV.class), new Op(SUM, Sum.class),
    new Op(TAB, Tab.class), new Op(TAG, Tag.class), new Op(TAS, TAs.class),
    new Op(TBE, TbE.class), new Op(TXT, Txt.class), new Op(TRP, TrP.class),
    new Op(TRM, TrM.class), new Op(TRC, Trc.class), new Op(TYP, Typ.class),
    new Op(UNI, Uni.class)
  };

  /** Private constructor, preventing class instantiation. */
  private OprB() { }
  
  /**
   * Returns an operator instance for the specified operator type.
   * @param k node kind
   * @return operator
   * @throws QueryException query exception
   */
  static Opr e(final byte[] k) throws QueryException {
    try {
      for(int e = 0; e < OP.length; e++) {
        if(Token.eq(k, OP[e].nm)) return OP[e].op.newInstance();
      }
    } catch(final Exception ex) {
      final QueryException qe = new QueryException(ex.toString());
      qe.initCause(ex);
      throw qe;
    }
    throw new QueryException(PFSIMPLE, k);
  }
  
  /**
   * Auxiliary operator structure.
   */
  private static final class Op {
    /** Operator. */
    final Class<? extends Opr> op;
    /** Name of operator. */
    final byte[] nm;
    
    /**
     * Constructor.
     * @param o operator
     * @param n name
     */
    Op(final byte[] n, final Class<? extends Opr> o) {
      op = o;
      nm = n;
    }
  }
}
