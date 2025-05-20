package org.basex.query.func.fn;

import org.basex.build.html.*;
import org.basex.build.html.HtmlParser.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnParseHtml extends ParseHtml {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc);
  }

  @Override
  protected final Parser parser() {
    return HtmlParser.Parser.NU;
  }
}
