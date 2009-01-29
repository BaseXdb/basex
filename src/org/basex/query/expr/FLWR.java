package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
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
  public Expr comp(final QueryContext ctx) throws QueryException {
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
      return where != null ? new If(where, expr, Seq.EMPTY) : expr;
    }

    /* [CG] add where clause as predicate to last for clause
     * and remove variable calls.
     * Problem: where ....[$i = ...] -> ....[. = ...]
    if(fl[fl.length - 1] instanceof For || fl.length == 1) {
      final For f = (For) fl[fl.length - 1];
      if(where != null && f.pos == null && f.score == null &&
          f.expr instanceof AxisPath) {
        final Return t = where.returned(ctx);
        if(!t.num) {
          ctx.compInfo(OPTWHERE);
          f.expr = ((AxisPath) f.expr).addPred(where.removeVar(f.var));
          where = null;
          return comp(ctx);
        }
      }
    }
     */
    
    // simplify simple return expression
    if(where == null && fl.length == 1 && expr instanceof VarCall) {
      final Var v = ((VarCall) expr).var;
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

  /*
   * Normalize Expressions. 
   * Tries to resolve expressions nested For Loops.
   * @return boolean true, if anything was normalized
  boolean normalize() {
    boolean n = false;
      ForLet[] tmp = new ForLet[0];
      Step last = null;
      Var v = null;
      for (int f = 0; f != fl.length; f++) {
        if (fl[f] instanceof For) {
          final For fe = (For) fl[f];
          v = v == null ? fe.var : v;
          if (v != fe.var) {
            v = null;
            break;
          }
          if (fe.expr  instanceof AxisPath 
              && ((AxisPath) fe.expr).root != null) {
            AxisPath ap = (AxisPath) fe.expr;
            tmp = Array.add(tmp, new For(ap.root, fe.var, fe.pos, fe.score));
            tmp = Array.add(tmp, ap.convSteps(fe.var, fe.pos, fe.score));
            last = ap.step[ap.step.length - 1];
            n = true;
          } else tmp = Array.add(tmp, fe);
        } else tmp = Array.add(tmp, fl[f]);
      }
      
      if (where != null && last != null && v != null) {
        if (where instanceof CmpG) {
          final CmpG c = (CmpG) where;
          VarCall[] vc = ((CmpG) where).getVarCalls();
          if (vc.length == 1 && vc[0].eq(v)) {
            c.removeVarCall();
          }
        }

        if (last.pred == null) {
          last.pred = new Expr[]{where};
        } else {
          last.pred = Array.add(last.pred, where);
        }
        where = null;
      }
      
      if (expr != null && expr instanceof AxisPath && last != null 
          && v != null) {
        AxisPath ap = (AxisPath) expr;
        ForLet[] rtmp = new ForLet[0];
        VarCall[] vc = ap.getVarCalls();
        if (vc.length == 1 && vc[0].eq(v)) {
          // scoring?
          rtmp = Array.add(rtmp, ap.convSteps(v, null, null));
        }
        FLWR flwr = new FLWR(new ForLet[]{rtmp[rtmp.length - 1]}, null, vc[0]);
        for (int i = rtmp.length - 2; i > -1; i--) {
          flwr = new FLWR(new ForLet[]{rtmp[i]}, null, flwr);          
        }
        expr = flwr;
      }
      
      fl = tmp;   
      return n;
  }
  
  /*
   * Optimize normalized expressions.
   * @param ap AxisPath
   * @param ret flag for existing return clause
   * @return Optimized Expression
  AxisPath optimize(final AxisPath ap, final boolean ret) {
    Step[] s = new Step[0];
    Expr r = null;
    for (int i = 0; i < fl.length; i++) {
      final DBNode db = fl[i].expr.dbroot();
      if (!ret && db != null) {
        r = fl[i].expr;
      } else if (fl[i].expr instanceof AxisPath) {
        final AxisPath t = (AxisPath) fl[i].expr;
        if (ap.root instanceof VarCall && t.step.length == 1) {
          s = Array.add(s, ap.step);
        }
      }
    }
    AxisPath apt = ap;
    if (apt == null) {
      apt = AxisPath.get(r, s);
    } else {
      apt.step = Array.add(apt.step, s);
    }
    return expr instanceof FLWR ? ((FLWR) expr).optimize(apt, true) : apt;
  }
   */
}
