package org.basex.query;

import static org.basex.util.Token.*;
import java.util.ArrayList;
import java.util.Stack;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.PathNode;
import org.basex.query.path.Axis;
import org.basex.query.path.Test;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class QuerySuggest extends QueryParser {
  /** Data reference. */
  private Data data;
  /** All current path nodes. */
  private Stack<ArrayList<PathNode>> stack;
  /** All current path nodes. */
  private ArrayList<PathNode> all;
  /** Current path nodes. */
  private ArrayList<PathNode> curr;
  /** Hide flag. */
  private boolean show;
  /** Last tag name. */
  private byte[] tag;

  /**
   * Constructor.
   * @param c QueryContext
   * @param context Context
   */
  public QuerySuggest(final QueryContext c, final Context context) {
    super(c);
    data = context.data();
    checkInit();
  }
  
  /**
   * Sorts and returns the query suggestions.
   * @return completions
   */
  public StringList complete() {
    final StringList sl = new StringList();
    if(show) {
      for(final PathNode n : curr) {
        final String nm = string(n.token(data));
        if(nm.length() != 0 && !sl.contains(nm)) sl.add(nm);
      }
      sl.sort(true);
    }
    return sl;
  }

  @Override
  protected void checkInit() {
    if(stack != null && !stack.empty()) return;
    all = data.path.root();
    curr = all;
    stack = new Stack<ArrayList<PathNode>>();
  }

  @Override
  protected void checkAxis(final Axis axis) {
    if(axis == Axis.CHILD || axis == Axis.DESC) {
      all = data.path.desc(curr, axis == Axis.DESC);
    } else {
      all = new ArrayList<PathNode>();
    }
    curr = all;
    show = true;
  }

  @Override
  protected void checkTest(final Test test, final boolean attr) {
    final TokenBuilder tb = new TokenBuilder();
    if(attr) tb.add('@');
    if(test != null) tb.add(test.toString().replaceAll("\\*:?", ""));
    tag = tb.finish();
    checkTest(false);
  }

  /**
   * Checks the tag name.
   * @param eq equality test
   */
  private void checkTest(final boolean eq) {
    if(tag == null) return;
    
    final ArrayList<PathNode> tmp = new ArrayList<PathNode>();
    boolean s = false;
    for(final PathNode p : all) {
      final byte[] nm = p.token(data);
      if(startsWith(nm, tag)) {
        if(!eq || eq(nm, tag)) tmp.add(p);
        s |= !eq(tag, nm);
      }
    }
    show = tag.length == 0 || s;
    curr = tmp;
  }
  
  @Override
  protected void checkPred(final boolean open) {
    if(open) {
      checkTest(true);
      final ArrayList<PathNode> tmp = new ArrayList<PathNode>();
      for(final PathNode p : curr) tmp.add(p);
      stack.add(tmp);
      checkAxis(Axis.CHILD);
    } else {
      curr = stack.pop();
      show = false;
      all = curr;
    }
  }

  @Override
  void error(final Object[] err, final Object... arg) throws QueryException {
    final QueryException qe = new QueryException(err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
