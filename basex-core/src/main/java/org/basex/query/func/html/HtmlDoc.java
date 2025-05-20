package org.basex.query.func.html;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HtmlDoc extends HtmlParse {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return doc(qc);
    } catch(final QueryException ex) {
      throw error(ex);
    }
  }
}
