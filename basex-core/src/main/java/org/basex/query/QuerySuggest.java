package org.basex.query;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.expr.path.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class analyzes the current path and gives suggestions for code completions.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class QuerySuggest extends QueryParser {
  /** Data reference. */
  private final Data data;

  /** Stack of current path nodes. */
  private Stack<ArrayList<PathNode>> stack;
  /** All current path nodes. */
  private ArrayList<PathNode> all;
  /** Current path nodes. */
  private ArrayList<PathNode> current;
  /** Show or hide completions. */
  private boolean show;
  /** Last element name. */
  private byte[] name;

  /**
   * Constructor.
   * @param query query string
   * @param qc query context
   * @param data data reference
   */
  QuerySuggest(final String query, final QueryContext qc, final Data data) {
    super(query, null, qc, null);
    this.data = data;
    checkInit();
  }

  /**
   * Sorts and returns the query suggestions.
   * @param index of valid input
   * @return list of suggestions, followed by valid input string
   */
  StringList complete(final int index) {
    final StringList list = new StringList();
    if(show) {
      for(final PathNode node : current) {
        final String nm = string(node.token(data));
        if(!nm.isEmpty()) list.addUnique(nm);
      }
      list.sort();
    }
    return list.add(input.substring(0, index));
  }

  @Override
  void checkInit() {
    if(stack == null || stack.empty()) {
      stack = new Stack<>();
      all = data.paths.root();
      current = all;
    }
  }

  @Override
  void checkAxis(final Axis axis) {
    all = axis != Axis.CHILD && axis != Axis.DESCENDANT ?
      new ArrayList<>() : PathIndex.desc(current, axis == Axis.DESCENDANT);
    current = all;
    show = true;
  }

  @Override
  protected void checkTest(final Test test, final boolean element) {
    final TokenBuilder tb = new TokenBuilder();
    if(!element) tb.add('@');
    if(test != null) tb.add(test.toString(false).replace("*:", ""));
    name = tb.finish();
    // use inexact matching only if the element is at the end:
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
    current = tmp;
  }

  @Override
  protected void checkPred(final boolean open) {
    if(stack == null) return;
    if(open) {
      checkTest(true);
      stack.add(new ArrayList<>(current));
      checkAxis(Axis.CHILD);
    } else {
      current = stack.pop();
      show = false;
      all = current;
    }
  }

  @Override
  public QueryException error(final QueryError error, final InputInfo ii, final Object... arg) {
    return super.error(error, ii, arg).suggest(this);
  }
}
