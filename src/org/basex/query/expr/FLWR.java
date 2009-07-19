package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.util.Var;
import org.basex.util.Array;

/**
 * FLWR Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FLWR extends FLWOR {
  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param r return expression
   */
  public FLWR(final ForLet[] f, final Expr w, final Expr r) {
    super(f, w, null, r);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // add where clause to last for clause and remove variable calls.
    if(where != null) {
      final ForLet f = fl[fl.length - 1];
      if(f instanceof For && f.standard() && where.removable(f.var, ctx)) {
        if(!where.returned(ctx).num) {
          // converting where clauses to predicates
          ctx.compInfo(OPTWHERE);
          final Expr w = where.remove(f.var);
          if(f.expr instanceof AxisPath) {
            f.expr = ((AxisPath) f.expr).addPred(w);
          } else {
            fl[fl.length - 1].expr = new Pred(f.expr, w);
          }
          where = null;
        }
      }
    }

    final Expr ex = super.comp(ctx);
    if(ex != this) return ex;

    // remove let clauses with static contents
    for(int f = 0; f != fl.length; f++) {
      if(fl[f].var.expr != null) {
        ctx.compInfo(OPTVAR, fl[f].var);
        fl = Array.delete(fl, f--);
      }
    }

    // no clauses left: simplify expression
    if(fl.length == 0) {
      // replace FLWR with IF clause or pass on return clause
      ctx.compInfo(OPTFLWOR);
      return where != null ? new If(where, ret, Seq.EMPTY) : ret;
    }

    // simplify simple return expression
    if(where == null && fl.length == 1 && fl[0].standard() &&
        ret instanceof VarCall) {
      final Var v = ((VarCall) ret).var;
      if(v.type == null && fl[0].var.eq(v)) {
        ctx.compInfo(OPTFLWOR);
        return fl[0].expr.comp(ctx);
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      private Iter[] iter;
      private Iter ir;
      private int p;

      @Override
      public Item next() throws QueryException {
        if(iter == null) {
          iter = new Iter[fl.length];
          for(int f = 0; f < fl.length; f++) iter[f] = ctx.iter(fl[f]);
        }

        while(true) {
          if(ir != null) {
            final Item i = ir.next();
            if(i != null) return i;
            ir = null;
          } else {
            while(iter[p].next().bool()) {
              if(p + 1 != fl.length) {
                p++;
              } else if(where == null || where.ebv(ctx).bool()) {
                ir = ctx.iter(ret);
                break;
              }
            }
            if(ir == null && p-- == 0) return null;
          }
        }
      }
    };
  }
}
