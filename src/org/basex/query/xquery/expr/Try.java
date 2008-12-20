package org.basex.query.xquery.expr;

import java.io.IOException;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;

/**
 * Project specific try/catch expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Try extends Expr {
  /** Expression. */
  Expr exp;
  /** Catches. */
  Catch[] ctch;

  /**
   * Constructor.
   * @param t try expression
   * @param c catch expressions
   */
  public Try(final Expr t, final Catch[] c) {
    exp = t;
    ctch = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    exp = ctx.comp(exp);
    for(int c = 0; c < ctch.length; c++) ctch[c] = (Catch) ctch[c].comp(ctx);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      Iter it;

      @Override
      public Item next() throws XQException {
        try {
          if(it == null) it = ctx.iter(exp);
          return it.next();
        } catch(final XQException ex) {
          for(int c = 0; c < ctch.length; c++) {
            it = ctch[c].iter(ctx, ex);
            if(it != null) return it.next();
          }
          throw ex;
        }
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    exp.plan(ser);
    for(final Catch c : ctch) c.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("try { " + exp + "}");
    for(final Catch c : ctch) sb.append(" " + c);
    return sb.toString();
  }
}
