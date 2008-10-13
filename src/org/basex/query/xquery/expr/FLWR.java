package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;

/**
 * FLWR Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class FLWR extends Single {
  /** For/Let expressions. */
  protected final ForLet[] fl;
  /** Order Expressions. */
  protected Order order;
  /** Where Expression. */
  protected Expr where;

  /**
   * Constructor.
   * @param f variable inputs
   * @param w where clause
   * @param r return expression
   */
  public FLWR(final ForLet[] f, final Expr w, final Expr r) {
    super(r);
    fl = f;
    where = w;
  }

  @Override
  public final Expr comp(final XQContext ctx) throws XQException {
    final int vs = ctx.vars.size();
    for(int f = 0; f != fl.length; f++) {
      final Expr e = ctx.comp(fl[f]);
      if(e.e()) return Seq.EMPTY;
      fl[f] = (ForLet) e;
    }
    if(where != null) where = ctx.comp(where);
    if(order != null) order.comp(ctx);
    expr = ctx.comp(expr);
    ctx.vars.reset(vs);
    return this;
  }

  @Override
  @SuppressWarnings("unused")
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {
      private Iter[] iter;
      private Iter ir;
      private int p = 0;

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
              } else {
                if(where == null || ctx.iter(where).ebv().bool()) {
                  ir = ctx.iter(expr);
                  break;
                }
              }
            }
            if(ir == null) {
              if(p == 0) return null;
              --p;
            }
          }
        }
      }
      @Override
      public String toString() {
        return FLWR.this.toString();
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, EVAL, ITER);
    for(final ForLet f : fl) f.plan(ser);
    if(where != null) where.plan(ser);
    ser.openElement(RET);
    expr.plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public final String color() {
    return "99FF99";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i != fl.length; i++) sb.append((i != 0 ? " " : "") + fl[i]);
    if(where != null) sb.append(" where " + where);
    return sb.append(" return " + expr).toString();
  }
}
