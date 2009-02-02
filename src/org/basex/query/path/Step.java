package org.basex.query.path;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import java.util.HashSet;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.SkelNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Pos;
import org.basex.query.expr.Preds;
import org.basex.query.expr.Return;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.Test.Kind;
import org.basex.query.util.Err;
import org.basex.query.util.Scoring;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Location Step expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Step extends Preds {
  /** Axis. */
  public Axis axis;
  /** Node test. */
  public Test test;

  /**
   * This method creates a step without predicates.
   * @param a axis
   * @param t node test
   * @return step
   */
  public static Step get(final Axis a, final Test t) {
    return new SimpleIterStep(a, t);
  }

  /**
   * This method creates a step instance.
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
    super(p);
    axis = a;
    test = t;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(!test.comp(ctx)) return Seq.EMPTY;
    
    final Data data = ctx.data();
    ctx.leaf = false;
    if(data != null && test.kind == Kind.NAME) {
      final byte[] ln = ((NameTest) test).ln;
      final boolean att = test.type == Type.ATT;
      ctx.leaf = axis.down && data.meta.uptodate && data.ns.size() == 0 &&
        data.tags.stat(att ? data.attNameID(ln) : data.tagID(ln)).leaf;
    }
    final Expr e = super.comp(ctx);
    ctx.leaf = false;

    if(e != this) return Seq.EMPTY;

    // No predicates.. evaluate via simple iterator
    if(pred.length == 0) return get(axis, test);
    final Expr p = pred[0];
    
    // Position predicate
    final Pos pos = p instanceof Pos ? (Pos) p : null;
    // Last flag
    final boolean last = p instanceof Fun && ((Fun) p).func == FunDef.LAST;
    // Multiple Predicates or POS
    if(pred.length > 1 || !last && pos == null && usesPos(ctx)) return this;
    // Use iterative evaluation
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
          final Item i = p.test(ctx);
          if(i != null) {
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
   * Counts a single location step.
   * @param nodes input nodes
   * @param data data reference
   * @return node array
   */
  public HashSet<SkelNode> count(final HashSet<SkelNode> nodes,
      final Data data) {

    if(pred.length != 0) return null;
    int kind = -1;
    byte[] n = null;
    int name = 0;

    if(test.type != null) {
      kind = Nod.kind(test.type);
      if(kind == Data.PI) return null;

      if(test.kind == Kind.NAME) n = ((NameTest) test).ln;
      if(n == null && test.kind != null && test.kind != Kind.ALL) return null;
      name = n == null ? 0 : kind == Data.ELEM ? data.tagID(n) :
        kind == Data.ATTR ? data.attNameID(n) : 0;
    }
    final boolean desc = axis == Axis.DESC;
    if(!desc && axis != Axis.CHILD) return null;
    
    final HashSet<SkelNode> out = new HashSet<SkelNode>();
    for(final SkelNode sn : nodes) {
      data.skel.desc(sn, out, name, kind, desc);
    }
    return out;
  }
  
  /**
   * Adds a predicate to the step.
   * @param p predicate to be added
   * @return resulting step instance
   */
  public final Step addPred(final Expr p) {
    pred = Array.add(pred, p);
    return get(axis, test, pred);
  }

  /**
   * Adds a position predicate to the step.
   * @param ctx query context
   * @return resulting step instance or null
   */
  final Step addPos(final QueryContext ctx) {
    if(axis != Axis.PARENT && axis != Axis.SELF && pred.length == 0) {
      ctx.compInfo(OPTPOS);
      return addPred(Itr.get(1));
    }
    return this;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
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
  public Return returned(final QueryContext ctx) {
    return Return.NODSEQ;
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
    sb.append(super.toString());
    return sb.toString();
  }
}
