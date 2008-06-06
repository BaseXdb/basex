package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Arr;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
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
    return uses(Using.POS) ? this : expr.length == 0 ?
        new SimpleIterStep(axis, test, expr) : new IterStep(axis, test, expr);
  }

  @Override
  public NodeIter iter(final XQContext ctx) throws XQException {
    final Item ci = ctx.item;

    if(ci == null) Err.or(XPNODES, this);
    final Iter iter = ci.iter();

    final NodIter ni = new NodIter();
    NodIter nb = new NodIter();
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.or(NODESPATH, this, it);
      final NodeIter ir = axis.init((Node) it);
      Node nod;
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
        for(int e = 0; e != expr.length; e++) {
          final Type t = expr[e].returned();
          if(t == null || t.num || expr[e].uses(u)) return true;
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
  public void plan(final Serializer ser) throws Exception {
    ser.startElement(this);
    ser.attribute(AXIS, Token.token(axis.name));
    ser.attribute(TEST, Token.token(test.toString()));

    if(expr.length != 0) {
      ser.finishElement();
      for(Expr e : expr) e.plan(ser);
      ser.closeElement(this);
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
    final StringBuilder sb = new StringBuilder("Step(");
    if(test.type == Type.NOD) {
      if(axis == Axis.PARENT) return "..";
      if(axis == Axis.SELF) return ".";
    }
    if(axis == Axis.ATTR) sb.append("@");
    else if(axis != Axis.CHILD) sb.append(axis + "::");
    sb.append(test);
    for(final Expr e : expr) sb.append("[" + e + "]");
    return sb.append(")").toString();
  }
}
