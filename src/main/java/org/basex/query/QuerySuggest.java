package org.basex.query;

import static org.basex.util.Token.*;

import java.util.Stack;

import org.basex.data.Data;
import org.basex.index.path.PathNode;
import org.basex.query.path.Axis;
import org.basex.query.path.Test;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.list.ObjList;
import org.basex.util.list.StringList;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QuerySuggest extends QueryParser {
  /** Data reference. */
  private final Data data;
  /** All current path nodes. */
  private Stack<ObjList<PathNode>> stack;
  /** All current path nodes. */
  private ObjList<PathNode> all;
  /** Current path nodes. */
  private ObjList<PathNode> curr;
  /** Hide flag. */
  private boolean show;
  /** Last tag name. */
  private byte[] tag;

  /**
   * Constructor.
   * @param q query
   * @param c query context
   * @param d data reference
   * @throws QueryException query exception
   */
  public QuerySuggest(final String q, final QueryContext c, final Data d)
      throws QueryException {
    super(q, c);
    data = d;
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
        if(!nm.isEmpty() && !sl.contains(nm)) sl.add(nm);
      }
      sl.sort(true, true);
    }
    return sl;
  }

  @Override
  protected void checkInit() {
    if(stack != null && !stack.empty() || !data.meta.pathindex) return;
    all = data.pthindex.root();
    curr = all;
    stack = new Stack<ObjList<PathNode>>();
  }

  @Override
  protected void checkAxis(final Axis axis) {
    all = axis != Axis.CHILD && axis != Axis.DESC || !data.meta.pathindex ?
      new ObjList<PathNode>() : data.pthindex.desc(curr, axis == Axis.DESC);
    curr = all;
    show = true;
  }

  @Override
  protected void checkTest(final Test test, final boolean attr) {
    final TokenBuilder tb = new TokenBuilder();
    if(attr) tb.add('@');
    if(test != null) tb.add(test.toString().replaceAll("\\*:", ""));
    tag = tb.finish();
    // use inexact matching only, if the tag is at the end:
    checkTest(qp < ql);
  }

  /**
   * Checks the tag name.
   * @param eq equality test
   */
  private void checkTest(final boolean eq) {
    if(tag == null) return;

    final ObjList<PathNode> tmp = new ObjList<PathNode>();
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
    if(stack == null) return;
    if(open) {
      checkTest(true);
      final ObjList<PathNode> tmp = new ObjList<PathNode>();
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
  public QueryException error(final Err err, final Object... arg)
      throws QueryException {

    final QueryException qe = new QueryException(input(), err, arg);
    qe.complete(this, complete());
    throw qe;
  }
}
