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
public final class XsltVersion extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) {
    return Str.get(Xslt.VERSION);
  }
}
