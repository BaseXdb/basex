package org.basex.query.xpath;

import static org.basex.data.DataText.*;
import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.data.Nodes;
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
  /** Node reference. */
  Nodes curr;
  /** Current skeleton nodes. */
  ArrayList<Node> ctx = new ArrayList<Node>();
  /** Skeleton reference. */
  Skeleton skel;
  /** Last step. */
  Step last;

  /**
   * Constructor, specifying a node set.
   * @param q query
   * @param n context nodes
   */
  public XPSuggest(final String q, final Nodes n) {
    super(q);
    curr = n;
    skel = curr.data.skel;
  }

  @Override
  LocPath absLocPath(final LocPath path) throws QueryException {
    // <CG> move current context to stack..
    ctx = new ArrayList<Node>();
    ctx.add(skel.root());
    final LocPath lp = super.absLocPath(path);
    filter(false);
    return lp;
  }

  @Override
  LocPath relLocPath(final LocPath path) throws QueryException {
    return super.relLocPath(path);
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
    for(int c = ctx.size() - 1; c >= 0; c--) {
      final Node n = ctx.get(c);
      final byte[] t = n.token(curr.data);
      if(n.kind == Data.ELEM && tn == TestName.ALLNODES) continue;
      
      final boolean eq = Token.eq(t, tn);
      if(finish) {
        if(!eq) ctx.remove(c);
      } else {
        if(eq || !Token.startsWith(t, tn)) ctx.remove(c);
      }
    }
  }
  
  @Override
  Step step() throws QueryException {
    final Step step = super.step();
    filter(true);
    if(step == null) {
      ctx = skel.child(ctx, 0, false);
      return null;
    }

    if(step.axis == Axis.CHILD) {
      ctx = skel.child(ctx, 0, false);
    } else if(step.axis == Axis.DESC || step.axis == Axis.DESCORSELF) {
      ctx = skel.child(ctx, 0, true);
    } else {
      ctx = new ArrayList<Node>();
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

    for(final Node r : ctx) {
      final String name = Token.string(r.token(curr.data));
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
    if(s.test instanceof TestName) return ((TestName) s.test).name;
    return Token.EMPTY;
  }
  
  @Override
  Expr error(final String err, final Object... arg) throws QueryException {

    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
