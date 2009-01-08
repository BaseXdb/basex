package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Array;
import org.basex.query.xquery.path.AxisPath;
import org.basex.query.xquery.path.Step;
import org.basex.query.xquery.util.Var;

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
        ctx.compInfo(OPTSTAT, fl[f].var);
        fl = Array.delete(fl, f--);
      }
    }
  
    // no clauses left: simplify expression
    if(fl.length == 0) {
      // replace FLWR with IF clause or pass on return clause
      if(where != null) expr = new If(where, expr, Seq.EMPTY);
      ctx.compInfo(OPTFLWOR);
      return expr;
    }
    //final boolean n = normalize(ctx);
    return this;
    //return n ? optimize(null, false) : this;
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

  /**
   * Normalize Expressions. 
   * Tries to resolve expressions nested For Loops.
   * @return boolean true, if anything was normalized
   */
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
  
  /**
   * Optimize normalized expressions.
   * @param ap AxisPath
   * @param ret flag for existing return clause
   * @return Optimized Expression
   */
  AxisPath optimize(final AxisPath ap, final boolean ret) {
    Step[] s = new Step[0];
    Expr r = null;
    for (int i = 0; i < fl.length; i++) {
      if (!ret && fl[i].expr instanceof DBNode) {
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
}
