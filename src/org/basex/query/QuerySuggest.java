package org.basex.query;

import static org.basex.data.DataText.*;

import java.util.ArrayList;
import java.util.Stack;

import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.PathNode;
import org.basex.data.PathSummary;
import org.basex.query.item.Type;
import org.basex.query.path.Axis;
import org.basex.query.path.NameTest;
import org.basex.query.path.Test;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class QuerySuggest extends QueryParser {
  /** Context. */
  Context ctx;
  /** Current path summary nodes. */
  Stack<ArrayList<PathNode>> stack = new Stack<ArrayList<PathNode>>();
  /** Path summary. */
  PathSummary skel;
  /** Last axis. */
  Axis laxis;
  /** Last test. */
  Test ltest;

  // [CG] Suggest/Check

  /**
   * Constructor.
   * @param c QueryContext
   * @param context Context
   */
  public QuerySuggest(final QueryContext c, final Context context) {
    super(c);
    ctx = context;
    skel = ctx.data().path;
  }

  @Override
  void sugPath(final int type) {
    if (type == 0) {
      //System.out.println("absLocPath");
      final ArrayList<PathNode> list = new ArrayList<PathNode>();
      list.add(skel.root);
      stack.push(list);
    } else if (type == 1) {
      //System.out.println("relLocPath");
      ArrayList<PathNode> list = null;
      if(stack.size() == 0) {
        if(!ctx.root()) return;
        list = new ArrayList<PathNode>();
        list.add(skel.root);
      } else {
        list = skel.desc(stack.peek(), 0, Data.ELEM, false);
      }
      stack.push(list);
    }
  }
  
  @Override
  void pred() {
    final int s = stack.size();
    //System.out.println("pred: " + s);
    while(stack.size() != s) stack.pop();
  }

  @Override
  void checkStep(final Axis axis, final Test test) {
    //System.out.println("checkStep: " + axis + " " + test);
    filter(true);
    if(axis == null) {
      if(!stack.empty()) stack.push(skel.desc(stack.pop(), 0,
          Data.ELEM, false));
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
  public void filter(final boolean finish) {
    //System.out.println("Filter: " + finish);
    if(laxis == null) return;
    if(finish && ltest == Test.NODE) return;
    final byte[] tn = entry(laxis, ltest);
    if(tn == null) return;

    // [AW] temporarily added to skip Exception after input of "//*["
    if(stack.empty()) return;

    final ArrayList<PathNode> list = stack.peek();
    for(int c = list.size() - 1; c >= 0; c--) {
      final PathNode n = list.get(c);
      if(n.kind == Data.ELEM && tn == new byte[] { '*'}) continue;
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
    //System.out.println("complete");
    final StringList sl = new StringList();
    if(stack.empty()) return sl;
    for(final PathNode r : stack.peek()) {
      final String name = Token.string(r.token(ctx.data()));
      if(name.length() != 0 && !sl.contains(name)) sl.add(name);
    }
    sl.sort(true);
    return sl;
  }

  /**
   * Returns a node entry.
   * @param a axis
   * @param t text
   * @return completion
   */
  private byte[] entry(final Axis a, final Test t) {
    //System.out.println("entry: " + a + " " + t);
    if(t.type == Type.TXT) {
      return TEXT;
    }
    if(t.type == Type.COM) {
      return COMM;
    }
    if(t.type == Type.PI) {
      return PI;
    }
    if(t instanceof NameTest && t.name != null) {
      final byte[] name = t.name.ln();
      return a == Axis.ATTR ? Token.concat(ATT, name) : name;
    }
    return Token.EMPTY;
  }

  @Override
  void error(final Object[] err, final Object... arg) throws QueryException {
    //System.out.println("error");
    final QueryException qe = new QueryException(err, arg);
    mark();
    if (qe.simple().startsWith("Unexpected") ||
        qe.simple().equals("Expecting attribute name.")) filter(false);
    qe.complete(this, complete());
    throw qe;
  }
}
