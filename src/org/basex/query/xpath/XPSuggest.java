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
import org.basex.query.xpath.locpath.Test;
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
  private Context ctx;
  /** Current skeleton nodes. */
  private Stack<ArrayList<Node>> stack = new Stack<ArrayList<Node>>();
  /** Skeleton reference. */
  private Skeleton skel;
  /** Last axis. */
  private Axis laxis;
  /** Last test. */
  private Test ltest;

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
    return lp;
  }

  @Override
  LocPath relLocPath(final LocPath path) throws QueryException {
    ArrayList<Node> list = null;
    if(stack.size() == 0) {
      if(!ctx.root()) return super.relLocPath(path);
      list = new ArrayList<Node>();
      list.add(skel.root());
    } else {
      list = skel.desc(stack.peek(), 0, Data.ELEM, false);
    }

    stack.push(list);
    final LocPath lp = super.relLocPath(path);
    filter(false);
    return lp;
  }

  @Override
  Expr pred() throws QueryException {
    final int s = stack.size();
    final Expr expr = super.pred();
    while(stack.size() != s) stack.pop();
    return expr;
  }

  @Override
  void checkStep(final Axis axis, final Test test) {
    filter(true);
    if(axis == null) {
      stack.push(skel.desc(stack.pop(), 0, Data.ELEM, false));
      return;
    }

    if(axis == Axis.CHILD) {
      stack.push(skel.desc(stack.pop(), 0, Data.ELEM, false));
    } else if(axis == Axis.ATTR) {
      stack.push(skel.desc(stack.pop(), 0, Data.ATTR, false));
    } else if(axis == Axis.DESC || axis == Axis.DESCORSELF) {
      stack.push(skel.desc(stack.pop(), 0, Data.ELEM, true));
    } else {
      stack.peek().clear();
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
    if(finish && ltest == TestNode.NODE) return;
    final byte[] tn = entry(laxis, ltest);
    if(tn == null) return;
    final ArrayList<Node> list = stack.peek();
    for(int c = list.size() - 1; c >= 0; c--) {
      final Node n = list.get(c);
      if(n.kind == Data.ELEM && tn == TestName.ALLNODES) continue;

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
   * @param a axis
   * @param t text
   * @return completions
   */
  private byte[] entry(final Axis a, final Test t) {
    if(t == TestNode.TEXT) return TEXT;
    if(t == TestNode.COMM) return COMM;
    if(t == TestNode.PI) return PI;
    if(t instanceof TestName) {
      final byte[] name = ((TestName) t).name;
      return a == Axis.ATTR ? Token.concat(ATT, name) : name;
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
