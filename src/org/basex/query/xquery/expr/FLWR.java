package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Array;

/**
 * FLWR Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public Expr comp(final XQContext ctx) throws XQException {
    final Expr ex = super.comp(ctx);
    if(ex != this) return ex;
    
    // remove let clauses with static contents
    for(int f = 0; f != fl.length; f++) {
      if(fl[f].var.expr != null) {
        ctx.compInfo(OPTSTAT, fl[f]);
        fl = Array.delete(fl, f--);
      }
    }
  
    // no clauses left: simplify expression
    if(fl.length == 0) {
      // replace FLWR with IF clause or pass on return clause
      if(where != null) expr = new If(where, expr, Seq.EMPTY);
      ctx.compInfo(OPTSIMPLE, this, expr);
      return expr;
    }
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      private Iter[] iter;
      private Iter ir;
      private int p;

      @Override
      public Item next() throws XQException {
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
              } else if(where == null || ctx.iter(where).ebv().bool()) {
                ir = ctx.iter(expr);
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
