package org.basex.query.func.html;

import org.basex.build.html.HtmlParser.*;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class HtmlParse extends ParseHtml {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc);
  }

  @Override
  protected final Parser parser() {
    return Parser.PARSER;
  }
}
