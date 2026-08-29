package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltInit extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    Xslt.init();
    return Empty.VALUE;
  }
}
