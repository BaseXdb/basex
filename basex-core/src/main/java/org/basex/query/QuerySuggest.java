package org.basex.query;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QuerySuggest extends QueryParser {
  /** Data reference. */
  private final Data data;
  /** All current path nodes. */
  private Stack<ArrayList<PathNode>> stack;
  /** All current path nodes. */
  private ArrayList<PathNode> all;
  /** Current path nodes. */
  private ArrayList<PathNode> curr;
  /** Hide flag. */
  private boolean show;
  /** Last element name. */
  private byte[] name;

  /**
   * Constructor.
   * @param query query string
   * @param qc query context
   * @param data data reference
   * @throws QueryException query exception
   */
  public QuerySuggest(final String query, final QueryContext qc, final Data data)
      throws QueryException {
    super(query, null, qc, null);
    this.data = data;
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
      sl.sort();
    }
    return sl;
  }

  @Override
  void checkInit() {
    if(stack != null && !stack.empty()) return;
    all = data.paths.root();
    curr = all;
    stack = new Stack<>();
  }

  @Override
  void checkAxis(final Axis axis) {
    all = axis != Axis.CHILD && axis != Axis.DESC ?
      new ArrayList<PathNode>() : PathSummary.desc(curr, axis == Axis.DESC);
    curr = all;
    show = true;
  }

  @Override
  protected void checkTest(final Test test, final boolean attr) {
    final TokenBuilder tb = new TokenBuilder();
    if(attr) tb.add('@');
    if(test != null) tb.add(test.toString().replaceAll("\\*:", ""));
    name = tb.finish();
    // use inexact matching only, if the element is at the end:
    checkTest(pos < length);
  }

  /**
   * Checks the element name.
   * @param eq equality test
   */
  private void checkTest(final boolean eq) {
    if(name == null) return;

    final ArrayList<PathNode> tmp = new ArrayList<>();
    boolean s = false;
    for(final PathNode p : all) {
      final byte[] nm = p.token(data);
      if(startsWith(nm, name)) {
        if(!eq || eq(nm, name)) tmp.add(p);
        s |= !eq(name, nm);
      }
    }
    show = name.length == 0 || s;
    curr = tmp;
  }

  @Override
  protected void checkPred(final boolean open) {
    if(stack == null) return;
    if(open) {
      checkTest(true);
      final ArrayList<PathNode> tmp = new ArrayList<>();
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
  public QueryException error(final Err err, final Object... arg) {
    return err.get(info(), arg).suggest(this, complete());
  }
}
