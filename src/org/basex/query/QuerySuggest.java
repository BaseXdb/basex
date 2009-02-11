package org.basex.query;

import static org.basex.data.DataText.*;

import java.util.ArrayList;
import java.util.Stack;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.SkelNode;
import org.basex.data.Skeleton;
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
  /** Last axis. */
  static Axis tempA;
  /** Last test. */
  static Test tempT;
  /** Help for filter. */
  static int filter = 0;
  /** Help for filter. */
  static boolean doFilt;

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
  void absPath(final int s, final Axis axis, final Test test) {
    //System.out.println("absLocPath: " + s);
    if (s > 0) {
      final ArrayList<SkelNode> list = new ArrayList<SkelNode>();
      list.add(skel.root);
      stack.push(list);
      if(s == 1) {
        checkStep(axis, test);
        if (filter <= 1) filter(false);
        filter++;
      } else if(s == 2) {
        if(axis != null && test != null) doFilt = true;
        checkStep(Axis.DESCORSELF, Test.NODE);
        if (tempT != null) {
          if (tempT.sameAs(test)) {
            doFilt = false;
          } else {
            checkStep(tempA, tempT);
          }
        }
        tempA = axis;
        tempT = test;
        checkStep(axis, test);
        if (doFilt) filter(false);
      }
    } else {
      checkStep(axis, test);
    }
  }

  /**
   * Performs optional step checks.
   * @param axis axis
   * @param test test
   */
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
  private void filter(final boolean finish) {
    //System.out.println("Filter: " + finish);
    if(laxis == null) return;
    if(finish && ltest == Test.NODE) return;
    final byte[] tn = entry(laxis, ltest);
    if(tn == null) return;

    // [AW] temporarily added to skip Exception after input of "//*["
    if(stack.empty()) return;

    final ArrayList<SkelNode> list = stack.peek();
    for(int c = list.size() - 1; c >= 0; c--) {
      final SkelNode n = list.get(c);
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
    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
