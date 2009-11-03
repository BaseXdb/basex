package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.util.Set;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.DBNode;
import org.basex.query.item.FNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.ReplaceElemContent;
import org.basex.query.up.primitives.ReplacePrimitive;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.Err;
import static org.basex.util.Token.*;

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
        ctx.updates.add(new ReplacePrimitive(n, aSeq, true), ctx);
      }
      return Seq.EMPTY;
    }
    
    // replace value of node
    final byte[] txt = seq.size() < 1 ? EMPTY : seq.get(0).str();
    if(k == Data.COMM && (contains(txt, token("--")) || 
        endsWith(txt, token("-")))) Err.or(COMINVALID, i);
    if(k == Data.PI && (contains(txt, token("?>")) || 
        endsWith(txt, token("-")))) Err.or(CPICONT, i);
    ctx.updates.add(k == Data.ELEM ? 
        new ReplaceElemContent(n, txt) : new ReplaceValue(n, txt), ctx);
    return Seq.EMPTY;
  }
  
  /**
   * Checks for duplicates/namespace conflicts in the given set. 
   * @param s set
   * @param n node ns to add
   * @return true if duplicates exist
   */
  public static boolean checkNS(final Set<String> s, final Nod n) {
    if(n instanceof FNode) return !s.add(string(((FNode) n).nname()));
    final DBNode dn = (DBNode) n;
    return !s.add(string(dn.data.attName(dn.pre)));
  }

  @Override
  public String toString() {
    return REPLACE + ' ' + (value ? VALUEE + ' ' + OF : "") +
      ' ' + NODE +  ' ' + expr[0] + ' ' + WITH + ' ' + expr[1];
  }
}
