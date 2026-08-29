package org.basex.query.func.inspect;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectModule extends StandardFunc {
  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final IOContent content = toContent(toString(arg(0), qc), qc);
    return new PlainDoc(qc, info).parse(content);
  }
}
