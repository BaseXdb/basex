package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltProcessor extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) {
    return Str.get(Xslt.PROCESSOR);
  }
}
