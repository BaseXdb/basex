package org.basex.query.func.html;

import static org.basex.query.QueryError.*;

import org.basex.build.html.HtmlParser;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class FnParseHtml extends HtmlParse {

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    String className = HtmlParser.firstUnavailableClass();
    if (className != null) throw BASEX_CLASSPATH_X_X.get(info, definition.local(), className);
    return super.item(qc, ii);
  }
}
