package org.basex.query.path;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.PathNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Pos;
import org.basex.query.expr.Preds;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Kind;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Location Step expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Step extends Preds {
  /** Axis. */
  Axis axis;
  /** Node test. */
  public Test test;

  /**
   * This method creates a copy from the specified step.
   * @param s step to be copied
   * @return step
   */
  public static Step get(final Step s) {
    return get(s.axis, s.test, s.pred);
  }

  /**
   * This method creates a step instance.
   * @param a axis
   * @param t node test
   * @param p predicates
   * @return step
   */
  public static Step get(final Axis a, final Test t, final Expr... p) {
    return p.length == 0 ? new SimpleIterStep(a, t) : new Step(a, t, p);
  }

  /**
   * Constructor.
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  protected Step(final Axis a, final Test t, final Expr... p) {
    super(p);
    axis = a;
    test = t;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(!test.comp(ctx)) return Seq.EMPTY;

    final Data data = ctx.data();
    ctx.leaf = false;
    if(data != null && test.kind == Kind.NAME && test.type != Type.ATT) {
      final byte[] ln = ((NameTest) test).ln;
      ctx.leaf = axis.down && data.meta.uptodate && data.ns.size() == 0 &&
        data.tags.stat(data.tags.id(ln)).leaf;
    }
    final Expr e = super.comp(ctx);
    ctx.leaf = false;
    if(e != this) return e;

    // no predicates.. evaluate via simple iterator
    if(pred.length == 0) return get(axis, test);
    final Expr p = pred[0];

    // position predicate
    final Pos pos = p instanceof Pos ? (Pos) p : null;
    // last flag
    final boolean last = p instanceof Fun && ((Fun) p).func == FunDef.LAST;
    // multiple Predicates or POS
    if(pred.length > 1 || !last && pos == null && uses(Use.POS, ctx))
      return this;
    // use iterative evaluation
    return new IterStep(axis, test, pred, pos, last);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = checkCtx(ctx).iter();

    final NodIter ni = new NodIter();
    NodIter nb = new NodIter();
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.or(NODESPATH, Step.this, it.type);
      final NodeIter ir = axis.init((Nod) it);
      Nod nod;
      while((nod = ir.next()) != null) {
        if(test.eval(nod)) nb.add(nod.finish());
      }

      // evaluates predicates
      for(final Expr p : pred) {
        ctx.size = nb.size();
        ctx.pos = 1;
        int c = 0;
        for(int s = 0; s < nb.size(); s++) {
          ctx.item = nb.get(s);
          final Item i = p.test(ctx);
          if(i != null) {
            // assign score value
            nb.get(s).score(i.score());
            nb.item[c++] = nb.get(s);
          }
          ctx.pos++;
        }
        nb.size = c;
      }
      for(int n = 0; n < nb.size(); n++) ni.add(nb.get(n));
      nb = new NodIter();
    }
    return ni;
  }

  /**
   * Checks if this is a simple axis without predicates.
   * @param ax axis to be checked
   * @param name name/node test
   * @return result of check
   */
  public final boolean simple(final Axis ax, final boolean name) {
    return axis == ax && pred.length == 0 &&
      (name ? test.kind == Test.Kind.NAME : test == Test.NODE);
  }

  /**
   * Counts the number of results for this location step.
   * @param nodes input nodes
   * @param data data reference
   * @return node array, or {@code null} if size cannot be evaluated
   */
  final ArrayList<PathNode> size(final ArrayList<PathNode> nodes,
      final Data data) {

    if(pred.length != 0) return null;
    int kind = -1;
    byte[] n = null;
    int name = 0;

    if(test.type != null) {
      kind = Nod.kind(test.type);
      if(kind == Data.PI || kind == Data.ATTR) return null;

      if(test.kind == Kind.NAME) n = ((NameTest) test).ln;
      if(n == null) {
        if(test.kind != null && test.kind != Kind.ALL) return null;
      } else if(kind == Data.ELEM) {
        name = data.tags.id(n);
      }
    }
    final boolean desc = axis == Axis.DESC;
    if(!desc && axis != Axis.CHILD) return null;

    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode pn : nodes) data.path.desc(pn, out, name, kind, desc);
    return out;
  }

  /**
   * Adds a predicate to the step.
   * @param p predicate to be added
   * @return resulting step instance
   */
  final Step addPred(final Expr p) {
    pred = Array.add(pred, p);
    return get(axis, test, pred);
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Step)) return false;
    final Step st = (Step) cmp;
    if(pred.length != st.pred.length || axis != st.axis ||
        !test.sameAs(st.test)) return false;
    for(int p = 0; p < pred.length; p++) {
      if(!pred[p].sameAs(st.pred[p])) return false;
    }
    return true;
  }

  @Override
  public final SeqType returned(final QueryContext ctx) {
    return SeqType.NOD_ZM;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(AXIS, Token.token(axis.name));
    ser.attribute(TEST, Token.token(test.toString()));
    super.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    if(test == Test.NODE) {
      if(axis == Axis.PARENT) sb.append("..");
      if(axis == Axis.SELF) sb.append(".");
    }
    if(sb.length() == 0) {
      if(axis == Axis.ATTR) sb.append("@");
      else if(axis != Axis.CHILD) sb.append(axis + "::");
      sb.append(test);
    }
    return sb.append(super.toString()).toString();
  }
}
