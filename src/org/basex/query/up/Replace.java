package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CComm;
import org.basex.query.expr.CPI;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.ReplaceElemContent;
import org.basex.query.up.primitives.ReplacePrimitive;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.Err;

/**
 * Replace expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Replace extends Update {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param t target expression
   * @param r source expression
   * @param v replace value of
   */
  public Replace(final Expr t, final Expr r, final boolean v) {
    super(t, r);
    value = v;
  }

  @Override
  public Seq atomic(final QueryContext ctx) throws QueryException {
    final Constr c = new Constr(ctx, expr[1]);
    final NodIter seq = c.children;
    final NodIter aSeq = c.ats;
    if(c.errAtt) Err.or(UPNOATTRPER);
    if(c.duplAtt != null) Err.or(UPATTDUPL, c.duplAtt);
    
    final Iter t = SeqIter.get(expr[0].iter(ctx));
    Item i = t.next();
    // check target constraints
    if(i == null) Err.or(UPSEQEMP, i);
    final int k = Nod.kind(i.type);
    if(t.size() > 1 || !(i instanceof Nod) || k == Data.DOC) 
      Err.or(UPTRGMULT, i);
    final Nod n = (Nod) i;
    final Nod p = n.parent();
    if(p == null) Err.or(UPNOPAR, i);
    
    // replace node
    if(!value) {
      if(k != Data.ATTR) {
        // replace non-attribute node
        if(aSeq.size() > 0) Err.or(UPWRELM, i);
        ctx.updates.add(new ReplacePrimitive(n, seq, false), ctx);
      } else {
        // replace attribute node
        if(seq.size() > 0) Err.or(UPWRATTR, i);
//        if(!UpdateFunctions.checkAttNames(p.attr(), aSeq, string(n.nname())))
//          Err.or(UPATTDUPL, n.nname());
        ctx.updates.add(new ReplacePrimitive(n, aSeq, true), ctx);
      }
    } else {
      // replace value of node
      final byte[] txt = seq.size() < 1 ? EMPTY : seq.get(0).str();
      if(k == Data.COMM) CComm.check(txt);
      if(k == Data.PI) CPI.check(txt);
      ctx.updates.add(k == Data.ELEM ? new ReplaceElemContent(n, txt) :
        // [LK] rewritten to pass on QNm - probably wrong for comments etc.
        new ReplaceValue(n, new QNm(txt)), ctx);
    }
    return Seq.EMPTY;
  }
  
  @Override
  public String toString() {
    return REPLACE + ' ' + (value ? VALUEE + ' ' + OF : "") +
      ' ' + NODE +  ' ' + expr[0] + ' ' + WITH + ' ' + expr[1];
  }
}
