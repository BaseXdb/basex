package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.ReplacePrimitive;
import org.basex.query.util.Err;

/**
 * Replace expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Replace extends Arr {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param t target expression
   * @param e source expression
   * @param v replace value of
   */
  public Replace(final Expr t, final Expr e, final boolean v) {
    super(t, e);
    value = v;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(value) return Iter.EMPTY;
    final Iter t = expr[0].iter(ctx);
    Item i = t.next();
    // check target constraints
    if(i == null) Err.or(UPSEQEMP, t);
    if(t.next() != null) Err.or(UPTRGMULT, t);
    if(!(i instanceof Nod)) Err.or(UPTRGMULT, t);
    final Nod trgtN = (Nod) i;
    final Nod par = trgtN.parent();
    if(par == null || Nod.kind(par.type) == Data.DOC) Err.or(UPNOPAR, t);
    // check replace constraints
    final Iter r = SeqIter.get(expr[1].iter(ctx));
    i = r.next();
    if(value) {
      Err.or(UPIMPL, "foobanchu");
      if(r.next() != null) Err.or(UPTRGMULT, i);
      if(!(i instanceof Str || i instanceof QNm)) Err.or(UPDATE, i);
      return Iter.EMPTY;
    }
    
    final boolean trgIsAttr = Nod.kind(trgtN.type) == Data.ATTR ? true : false;
    while(i != null) {
      if((Nod.kind(i.type) == Data.ATTR) ^ trgIsAttr) Err.or(UPDATE, t);
      i = r.next();
    }
    r.reset();
    ctx.updates.addPrimitive(new ReplacePrimitive(trgtN, r, trgIsAttr));
    return Iter.EMPTY;
  }

  @Override
  public String toString() {
    return REPLACE + (value ? VALUEE + OF : "") + NODE + expr[0] +
    WITH + expr[1];
  }
}
