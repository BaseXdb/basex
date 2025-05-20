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
    try {
      return parse(qc);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }

  @Override
  protected final Parser parser() {
    return Parser.DEFAULT;
  }

  /**
   * Adapts the error code.
   * @param ex exception to be adapted
   * @return new exception
   */
  final QueryException error(final QueryException ex) {
    return ex;
  }
}
