package org.basex.query;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class analyzes the current path and gives suggestions for code completions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class QuerySuggest extends QueryParser {
  /** Path suggestions. */
  private final PathSuggestions suggestions;

  /**
   * Constructor.
   * @param query query string
   * @param qc query context
   * @param data data reference
   */
  QuerySuggest(final String query, final QueryContext qc, final Data data) {
    super(query, null, qc, null);
    suggestions = new PathSuggestions(data);
  }

  /**
   * Sorts and returns the query suggestions.
   * @param index of valid input
   * @return list of suggestions, followed by valid input string
   */
  StringList complete(final int index) {
    return suggestions.names().add(substring(0, index).toString());
  }

  @Override
  void checkInit() {
    suggestions.init();
  }

  @Override
  void checkAxis(final Axis axis) {
    suggestions.axis(axis);
  }

  @Override
  protected void checkTest(final ExprInfo ei, final boolean element) {
    final TokenBuilder tb = new TokenBuilder();
    if(!element) tb.add('@');
    if(ei instanceof final Test test) tb.add(test.toString(false).replace("*:", ""));
    // use inexact matching only if the element is at the end:
    suggestions.test(tb.finish(), pos < length);
  }

  @Override
  protected void checkPred(final boolean open) {
    suggestions.predicate(open);
  }

  @Override
  public QueryException error(final QueryError error, final InputInfo info, final Object... arg) {
    return super.error(error, info, arg).suggest(this);
  }
}
