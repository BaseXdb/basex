package org.basex.query;

import java.util.ArrayList;
import java.util.Stack;
import org.basex.core.Context;
import org.basex.data.SkelNode;
import org.basex.data.Skeleton;
import org.basex.query.path.Axis;
import org.basex.query.path.Test;

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
   */
  public QuerySuggest(final QueryContext c) {
    super(c);
  }
  
  /*
  @Override
  Expr path() throws QueryException {
    
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
  private void filter(final boolean finish) {
    if(laxis == null) return;
    if(finish && ltest == Test.NODE) return;
    final byte[] tn = entry(laxis, ltest);
    if(tn == null) return;
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
   * @return completions
  private byte[] entry(final Axis a, final Test t) {
    /*if(t == TestNode.TEXT) return TEXT;
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
    return null;
  }
  */
}
