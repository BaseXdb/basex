package org.basex.query.func.xslt;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltInit extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    Xslt.init();
    return Empty.VALUE;
  }
}
