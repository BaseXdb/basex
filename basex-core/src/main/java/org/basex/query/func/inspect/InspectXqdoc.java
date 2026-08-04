package org.basex.query.func.inspect;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectXqdoc extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final IOContent content = toContent(toString(arg(0), qc), qc);
    return new XQDoc(qc, info).parse(content);
  }
}
