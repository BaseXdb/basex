package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnHtmlDoc extends FnParseHtml {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return doc(qc);
  }
}
