package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * FLWR clause.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FLWR extends GFLWOR {
  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param r return expression
   * @param ii input info
   */
  FLWR(final ForLet[] f, final Expr w, final Expr r, final InputInfo ii) {
    super(f, w, null, null, r, ii);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final Expr ex = super.comp(ctx);
    if(ex != this) return ex;

    // simplify basic GFLWOR expression (for $i in A return $i -> A)
    if(fl.length == 1 && where == null && ret instanceof VarRef) {
      final Var v = ((VarRef) ret).var;
      if(v.type == null && fl[0].var.is(v)) {
        ctx.compInfo(OPTFLWOR);
        return fl[0].expr;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      private Iter[] iter;
      private Iter rtrn;
      private int p;

      @Override
      public Item next() throws QueryException {
        init();

        while(true) {
          if(rtrn != null) {
            final Item i = rtrn.next();
            if(i != null) return i;
            rtrn = null;
          } else {
            while(iter[p].next() != null) {
              if(p + 1 != fl.length) {
                ++p;
              } else if(where == null || where.ebv(ctx, input).bool(input)) {
                rtrn = ctx.iter(ret);
                break;
              }
            }
            if(rtrn == null && p-- == 0) return null;
          }
        }
      }

      @Override
      public boolean reset() {
        if(iter != null) {
          for(final Iter i : iter) i.reset();
          iter = null;
          rtrn = null;
          p = 0;
        }
        return true;
      }

      /** Initializes the iterator. */
      private void init() throws QueryException {
        if(iter != null) return;
        iter = new Iter[fl.length];
        for(int f = 0; f < fl.length; ++f) iter[f] = ctx.iter(fl[f]);
      }
    };
  }

  @Override
  Expr markTailCalls() {
    for(final ForLet f : fl) if(f instanceof For) return this;
    ret = ret.markTailCalls();
    return this;
  }
}
