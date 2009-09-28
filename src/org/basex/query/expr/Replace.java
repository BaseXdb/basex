package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
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
    final Iter r = expr[1].iter(ctx);
    final boolean trgIsAttr = Nod.kind(trgtN.type) == Data.ATTR ? true : false;
    i = r.next();
    while(i != null) {
      if((Nod.kind(i.type) == Data.ATTR) ^ trgIsAttr) Err.or(INCOMPLETE, t);
      i = r.next();
    }
    r.reset();
    // [LK] create mem data instance here
    // create mem data -> insert doc node r as root
    // iter -> node,doc,... -> insert each node,etc. into r
    // apply
    if(value) Err.or(UPIMPL, value);
    final MemData m = buildDB(null, r,  
        trgtN instanceof DBNode ? ((DBNode) trgtN).data : null);
    ctx.updates.addPrimitive(new ReplacePrimitive(trgtN, m));
    return Iter.EMPTY;
  }

  @Override
  public String toString() {
    return REPLACE + (value ? VALUEE + OF : "") + NODE + expr[0] +
    WITH + expr[1];
  }
}
