package org.basex.query.func.fn;

import org.basex.build.html.HtmlParser;
import org.basex.query.*;
import org.basex.query.func.html.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnHtmlDoc extends HtmlParse {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String source = toStringOrNull(arg(0), qc);
    return source != null ? parse(toIO(source), HtmlParser.Parser.NU, qc) : Empty.VALUE;
  }
}