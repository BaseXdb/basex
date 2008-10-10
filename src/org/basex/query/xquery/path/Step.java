package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Token;

/**
 * Location Step expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Step extends Arr {
  /** Axis. */
  public Axis axis;
  /** Node test. */
  public Test test;

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  public Step(final Axis a, final Test t, final Expr[] p) {
    super(p);
    axis = a;
    test = t;
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    // No predicates.. use simple evaluation
    if(expr.length == 0) return new SimpleIterStep(axis, test, expr);
    // LAST
    final boolean last = expr[0] instanceof Fun &&
      ((Fun) expr[0]).func == FunDef.LAST;
    // Numeric value
    final boolean num = expr[0].n();
    // Multiple Predicates or POS
    return expr.length > 1 || (!last && !num && uses(Using.POS)) ? this : 
      new IterStep(axis, test, expr, last, num);
  }

  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Iter iter = checkCtx(ctx);

    final NodIter ni = new NodIter();
    NodIter nb = new NodIter();
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.or(NODESPATH, this, it.type);
      final NodeIter ir = axis.init((Nod) it);
      Nod nod;
      while((nod = ir.next()) != null) {
        if(test.e(nod, ctx)) {
          nod = nod.finish();
          nod.score(Scoring.step(it.score()));
          nb.add(nod);
        }
      }

      // evaluates predicates
      for(final Expr p : expr) {
        ctx.size = nb.size;
        ctx.pos = 1;
        int c = 0;
        for(int s = 0; s < nb.size; s++) {
          ctx.item = nb.list[s];
          final Item i = ctx.iter(p).ebv();
          if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
            // assign score value
            nb.list[s].score(i.score());
            nb.list[c++] = nb.list[s];
          }
          ctx.pos++;
        }
        nb.size = c;
      }
      ni.add(nb.list, nb.size);
      nb = new NodIter();
    }
    //ctx.item = ci;
    return ni;
  }

  /**
   * Checks if this is a simple axis (node test, no predicates).
   * @param ax axis to be checked
   * @return result of check
   */
  public boolean simple(final Axis ax) {
    return axis == ax && test == Test.NODE && expr.length == 0;
  }

  @Override
  public boolean uses(final Using u) {
    switch(u) {
      case POS:
        for(final Expr e : expr) {
          final Type t = e.returned();
          if(t == null || t.num || e.uses(u)) return true;
        }
        return super.uses(u);
      default:
        return super.uses(u);
    }
  }

  @Override
  public Type returned() {
    return Type.NOD;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.startElement(this);
    ser.attribute(AXIS, Token.token(axis.name));
    ser.attribute(TEST, Token.token(test.toString()));

    if(expr.length != 0) {
      ser.finishElement();
      for(Expr e : expr) e.plan(ser);
      ser.closeElement();
    } else {
      ser.emptyElement();
    }
  }

  @Override
  public final String color() {
    return "FFFF66";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("");
    if(test == Test.NODE) {
      if(axis == Axis.PARENT) return "..";
      if(axis == Axis.SELF) return ".";
    }
    if(axis == Axis.ATTR) sb.append("@");
    else if(axis != Axis.CHILD) sb.append(axis + "::");
    sb.append(test);
    for(final Expr e : expr) sb.append("[" + e + "]");
    return sb.toString();
  }
}
