package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.Pred;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Location Step expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Step extends Expr {
  /** Axis. */
  public Axis axis;
  /** Node test. */
  public Test test;
  /** Predicates. */
  public Expr[] pred;

  /**
   * This method creates an expression for steps without predicates.
   * @param a axis
   * @param t node test
   * @return step
   */
  public static Step get(final Axis a, final Test t) {
    return new SimpleIterStep(a, t);
  }

  /**
   * This method creates an expression for steps with predicates.
   * @param a axis
   * @param t node test
   * @param p predicates
   * @return step
   */
  public static Step get(final Axis a, final Test t, final Expr[] p) {
    return p.length == 0 ? get(a, t) : new Step(a, t, p);
  }

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  protected Step(final Axis a, final Test t, final Expr... p) {
    axis = a;
    test = t;
    pred = p;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    pred = Pred.comp(pred, ctx);
    if(pred == null || !test.comp(ctx)) return Seq.EMPTY;

    // No predicates.. evaluate via simple iterator
    if(pred.length == 0) return get(axis, test);
    
    // Last flag
    final boolean last = pred[0] instanceof Fun &&
      ((Fun) pred[0]).func == FunDef.LAST;
    // Numeric value
    final boolean num = pred[0].i() && ((Item) pred[0]).n();
    // Multiple Predicates or POS
    if(pred.length > 1 || !last && !num && uses(Using.POS)) return this;
    // Use iterative evaluation
    return new IterStep(axis, test, pred, last, num);
  }
  
  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Iter iter = checkCtx(ctx);

    final NodIter ni = new NodIter();
    NodIter nb = new NodIter();
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.or(NODESPATH, Step.this, it.type);
      final NodeIter ir = axis.init((Nod) it);
      Nod nod;
      while((nod = ir.next()) != null) {
        if(test.eval(nod)) {
          nod = nod.finish();
          nod.score(Scoring.step(it.score()));
          nb.add(nod);
        }
      }

      // evaluates predicates
      for(final Expr p : pred) {
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
      for(int n = 0; n < nb.size; n++) ni.add(nb.list[n]);
      nb = new NodIter();
    }
    return ni;
  }

  /**
   * Checks if this is a simple axis (node test, no predicates).
   * @param ax axis to be checked
   * @return result of check
   */
  final boolean simple(final Axis ax) {
    return axis == ax && test == Test.NODE && pred.length == 0;
  }
  
  /**
   * Checks if this is a simple name axis (no predicates).
   * @param ax axis to be checked
   * @return result of check
   */
  public final boolean simpleName(final Axis ax) {
    return axis == ax && pred.length == 0 && test.kind == Test.Kind.NAME;
  }
  
  /**
   * Adds a predicate to the step.
   * @param p predicate to be added
   * @return resulting step instance
   */
  public Step addPred(final Expr p) {
    pred = Array.add(pred, p);
    return this;
  }

  @Override
  public final boolean uses(final Using u) {
    for(final Expr p : pred) if(p.uses(u)) return true;
    
    if(u == Using.POS) {
      for(final Expr p : pred) {
        final Type t = p.returned();
        if(t == null || t.num) return true;
      }
    }
    return false;
  }

  @Override
  public Type returned() {
    return Type.NOD;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Step)) return false;
    final Step st = (Step) cmp;
    if(pred.length != st.pred.length || axis != st.axis ||
        !test.sameAs(st.test)) return false;
    for(int s = 0; s < pred.length; s++) {
      if(!pred[s].sameAs(st.pred[s])) return false;
    }
    return true;
  }

  @Override
  public final String color() {
    return "FFFF66";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(AXIS, Token.token(axis.name));
    ser.attribute(TEST, Token.token(test.toString()));
    for(final Expr p : pred) p.plan(ser);
    ser.closeElement();
  }
  
  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder("");
    if(test == Test.NODE) {
      if(axis == Axis.PARENT) return "..";
      if(axis == Axis.SELF) return ".";
    }
    if(axis == Axis.ATTR) sb.append("@");
    else if(axis != Axis.CHILD) sb.append(axis + "::");
    sb.append(test);
    for(final Expr e : pred) sb.append("[" + e + "]");
    return sb.toString();
  }
}
