package org.basex.query;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import java.util.ArrayList;
import java.util.Stack;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.SkelNode;
import org.basex.data.Skeleton;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Root;
import org.basex.query.item.Type;
import org.basex.query.path.Axis;
import org.basex.query.path.AxisPath;
import org.basex.query.path.MixedPath;
import org.basex.query.path.NameTest;
import org.basex.query.path.Step;
import org.basex.query.path.Test;
import org.basex.query.util.Err;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class QuerySuggest extends QueryParser {
    /** Context. */
  Context ctx;
  /** Current skeleton nodes. */
  Stack<ArrayList<SkelNode>> stack = new Stack<ArrayList<SkelNode>>();
  /** Skeleton reference. */
  Skeleton skel;
  /** Last axis. */
  Axis laxis;
  /** Last test. */
  Test ltest;
  
  /**
   * Constructor.
   * @param c QueryContext
   * @param context Context
   */
  public QuerySuggest(final QueryContext c, final Context context) {
    super(c);
    ctx = context;
    skel = ctx.data().skel;
  }
  
  @Override
  public Expr path() throws QueryException {
    final int s = consume('/') ? consume('/') ? 2 : 1 : 0;
    final Expr ex = step(s);
    if(ex == null) {
      if(s > 1) error(PATHMISS);
      return s == 0 ? null : new Root();
    }

    final boolean slash = consume('/');
    final boolean step = ex instanceof Step;
    if(!slash && s == 0 && !step) return ex;

    Expr[] list = {};
    if(s == 2) list = add(list, descOrSelf());
    
    final Expr root = s > 0 ? new Root() : !step ? ex : null;
    if(root != ex) list = add(list, ex);

    if(slash) {
      do {
        if(consume('/')) list = add(list, descOrSelf());
        final Expr st = check(step(0), PATHMISS);
        if(!(st instanceof org.basex.query.expr.Context)) list = add(list, st);
      } while(consume('/'));
    }
    
    if(list.length == 0) return root;
    
    // check if all steps are axis steps
    boolean axes = true;
    final Step[] tmp = new Step[list.length];
    for(int l = 0; l < list.length; l++) {
      axes &= list[l] instanceof Step;
      if(axes) tmp[l] = (Step) list[l];
    }
    return axes ? AxisPath.get(root, tmp) : new MixedPath(root, list);
  }
  
  @Override
  Step axis(final int s) throws QueryException {
    if (s == 1) {
      absPather("root");
      checkStep(Axis.CHILD, test(false));
    } else if (s == 2) {
      absPather("root");
      checkStep(Axis.DESC, test(false));
    }
    Axis ax = null;
    Test test = null;

    if(consumeWS2(DOT2)) {
      ax = Axis.PARENT;
      test = Test.NODE;
    } else if(consume('@')) {
      ax = Axis.ATTR;
      test = test(true);
      if(test == null) Err.or(NOATTNAME);
    } else {
      for(final Axis a : Axis.values()) {
        if(consumeWS(a.name, COL2, NOLOCSTEP)) {
          consumeWS2(COL2);
          alter = NOLOCSTEP;
          ap = qp;
          ax = a;
          test = test(a == Axis.ATTR);
          break;
        }
      }
    }
    if(ax == null) {
      ax = Axis.CHILD;
      test = test(false);
      if(test != null && test.type == Type.ATT) ax = Axis.ATTR;
    }
    if(test == null) return null;

    Expr[] pred = {};
    while(consumeWS2(BR1)) {
      pred = add(pred, expr());
      check(BR2);
    }
    return Step.get(ax, test, pred);
  }
  
  /**
   * Adds Nodes to the Path.
   * @param name Name of the test
   */
  void absPather(final String name) {
    if (name.equals("root")) {
      final ArrayList<SkelNode> list = new ArrayList<SkelNode>();
      list.add(skel.root);
      stack.push(list);
    }
  }

  @Override
  void checkStep(final Axis axis, final Test test) {
    filter(true);
    if(axis == null) {
      if(!stack.empty())
        stack.push(skel.desc(stack.pop(), 0, Data.ELEM, false));
      return;
    }

    // [CG] Suggest/check when stack is empty at this stage
    if(!stack.empty()) {
      if(axis == Axis.CHILD) {
        stack.push(skel.desc(stack.pop(), 0, Data.ELEM, false));
      } else if(axis == Axis.ATTR) {
        stack.push(skel.desc(stack.pop(), 0, Data.ATTR, false));
      } else if(axis == Axis.DESC || axis == Axis.DESCORSELF) {
        stack.push(skel.desc(stack.pop(), 0, Data.ELEM, true));
      } else {
        stack.peek().clear();
      }
    }
    laxis = axis;
    ltest = test;
  }

  /**
   * Filters the current steps.
   * @param finish finish flag
   */
  private void filter(final boolean finish) {
    if(laxis == null) return;
    if(finish && ltest == Test.NODE) return;
    final byte[] tn = entry(laxis, ltest);
    if(tn == null) return;
    
    // [AW] temporarily added to skip Exception after input of "//*["
    if(stack.empty()) return;
    
    final ArrayList<SkelNode> list = stack.peek();
    for(int c = list.size() - 1; c >= 0; c--) {
      final SkelNode n = list.get(c);
      if(n.kind == Data.ELEM && tn == new byte[] { '*' }) continue;

      final byte[] t = n.token(ctx.data());
      final boolean eq = Token.eq(t, tn);
      if(finish) {
        if(!eq) list.remove(c);
      } else {
        if(eq || !Token.startsWith(t, tn)) list.remove(c);
      }
    }
  }

  /**
   * Returns the code completions.
   * @return completions
   */
  public StringList complete() {
    final StringList sl = new StringList();
    if(stack.empty()) return sl;
    for(final SkelNode r : stack.peek()) {
      final String name = Token.string(r.token(ctx.data()));
      if(name.length() != 0 && !sl.contains(name)) sl.add(name);
    }
    sl.sort();
    return sl;
  }
  
  /**
   * Returns a node entry.
   * @param a axis
   * @param t text
   * @return completion
   */
  private byte[] entry(final Axis a, final Test t) {
    if (t.type == Type.TXT) {
      return org.basex.data.DataText.TEXT;
    }
    if(t.type == Type.COM) {
      return org.basex.data.DataText.COMM;
    }
    if (t.type == Type.PI) {
      return org.basex.data.DataText.PI;
    }
    if(t instanceof NameTest && t.name != null) {
      final byte[] name = t.name.ln();
      return a == Axis.ATTR ? Token.concat(ATT, name) : name;
    }
    return Token.EMPTY;
  }
  
  @Override
  void error(final Object[] err, final Object... arg) throws QueryException {
    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
