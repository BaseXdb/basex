package org.basex.query.xpath;

import static org.basex.data.DataText.*;
import java.util.ArrayList;
import java.util.Stack;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Skeleton;
import org.basex.data.Skeleton.Node;
import org.basex.query.QueryException;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.locpath.TestName;
import org.basex.query.xpath.locpath.TestNode;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XPSuggest extends XPParser {
  /** Context. */
  Context ctx;
  /** Current skeleton nodes. */
  Stack<ArrayList<Node>> stack = new Stack<ArrayList<Node>>();
  /** Skeleton reference. */
  Skeleton skel;
  /** Last step. */
  Step last;

  /**
   * Constructor, specifying a node set.
   * @param q query
   * @param c context
   */
  public XPSuggest(final String q, final Context c) {
    super(q);
    ctx = c;
    skel = ctx.data().skel;
  }

  @Override
  LocPath absLocPath(final LocPath path) throws QueryException {
    final ArrayList<Node> list = new ArrayList<Node>();
    list.add(skel.root());
    stack.push(list);
    final LocPath lp = super.absLocPath(path);
    filter(false);
    if(stack.size() > 1) {
      stack.pop();
    } 
    return lp;
  }

  @Override
  @SuppressWarnings("unchecked")
  LocPath relLocPath(final LocPath path) throws QueryException {
    ArrayList<Node> list = null;
    if(stack.size() == 0) {
      if(!ctx.root()) return super.relLocPath(path);
      list = new ArrayList<Node>();
      list.add(skel.root());
    } else {
      list = skel.child(stack.peek(), 0, false);
    }
    
    stack.push(list);
    final LocPath lp = super.relLocPath(path);
    filter(false);
    /*if(stack.size() > 1) {
      stack.pop();
    }*/
    return lp;
  }

  /**
   * Filters the current steps.
   * @param finish finish flag
   */
  void filter(final boolean finish) {
    if(last == null) return;
    if(finish && last.test == TestNode.NODE) return;
    final byte[] tn = entry(last);
    if(tn == null) return;
    final ArrayList<Node> list = stack.peek();
    for(int c = list.size() - 1; c >= 0; c--) {
      final Node n = list.get(c);
      final byte[] t = n.token(ctx.data());
      if(n.kind == Data.ELEM && tn == TestName.ALLNODES) continue;

      final boolean eq = Token.eq(t, tn);
      if(finish) {
        if(!eq) list.remove(c);
      } else {
        if(eq || !Token.startsWith(t, tn)) list.remove(c);
      }
    }
  }

  @Override
  Step step() throws QueryException {
    final Step step = super.step();
    if(stack.empty()) return step;

    filter(true);
    if(step == null) {
      stack.push(skel.child(stack.pop(), 0, false));
      return null;
    }

    if(step.axis == Axis.CHILD || step.axis == Axis.ATTR) {
      stack.push(skel.child(stack.pop(), 0, false));
    } else if(step.axis == Axis.DESC || step.axis == Axis.DESCORSELF) {
      stack.push(skel.child(stack.pop(), 0, true));
    } else {
      stack.peek().clear();
    }
    last = step;
    return step;
  }

  /**
   * Returns the code completions.
   * @return completions
   */
  public StringList complete() {
    final StringList sl = new StringList();
    if(stack.empty()) return sl;

    final ArrayList<Node> list = stack.peek();
    for(final Node r : list) {
      final String name = Token.string(r.token(ctx.data()));
      if(name.length() != 0 && !sl.contains(name)) sl.add(name);
    }
    sl.sort();
    return sl;
  }

  /**
   * Returns a node entry.
   * @param s step
   * @return completions
   */
  public byte[] entry(final Step s) {
    if(s.test == TestNode.TEXT) return TEXT;
    if(s.test == TestNode.COMM) return COMM;
    if(s.test == TestNode.PI) return PI;
    if(s.test instanceof TestName) {
      final byte[] name = ((TestName) s.test).name;
      return s.axis == Axis.ATTR ? Token.concat(ATT, name) : name;
    }
    return Token.EMPTY;
  }

  @Override
  Expr error(final String err, final Object... arg) throws QueryException {

    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
